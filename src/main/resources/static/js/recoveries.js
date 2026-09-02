let currentPage = 0;

let allCases = [];



document.addEventListener(
  "DOMContentLoaded",
  () => {

    refreshBtn.onclick =
      () => loadCases(currentPage);


    processDueBtn.onclick =
      processDue;


    statusFilter.onchange =
      renderCases;


    loadCases(0);
  }
);



async function loadCases(page) {

  currentPage =
    page;


  try {

    const response =
      await App.api(
        `/api/v1/recovery-cases?page=${page}&size=10`
      );


    allCases =
      response.content || [];


    renderCases();


    casesPager.replaceChildren(

      App.pager(

        App.pageMeta(response),

        loadCases
      )
    );


  } catch (error) {

    casesBody.innerHTML = `

      <tr>

        <td
          colspan="8"
          class="table-empty">

          Unable to load recovery cases.

        </td>

      </tr>

    `;


    App.toast(
      error.message,
      "error"
    );
  }
}



function renderCases() {

  const filter =
    statusFilter.value;


  const rows =
    filter
      ? allCases.filter(
          recovery =>
            recovery.status === filter
        )
      : allCases;


  casesBody.innerHTML =
    rows.length
      ? rows
          .map(
            recovery => {

              const confidence =
                recovery.confidence == null
                  ? 0
                  : Math.round(
                      Number(
                        recovery.confidence
                      ) * 100
                    );


              const actions =
                [];


              if (
                recovery.status ===
                "PENDING_ANALYSIS"
              ) {

                actions.push(
                  `<button
                    class="btn btn-primary btn-sm"
                    onclick="analyzeCase(${recovery.id})">
                    Analyze
                  </button>`
                );
              }


              if (
                recovery.status ===
                "RECOVERY_PLANNED"
              ) {

                actions.push(
                  `<button
                    class="btn btn-success btn-sm"
                    onclick="executeCase(${recovery.id})">
                    Execute
                  </button>`
                );
              }


              if (
                recovery.recommendedAction ===
                "RETRY_LATER"
                &&
                [
                  "RECOVERY_PLANNED",
                  "RECOVERY_IN_PROGRESS"
                ].includes(
                  recovery.status
                )
              ) {

                actions.push(
                  `<button
                    class="btn btn-secondary btn-sm"
                    onclick="scheduleCase(${recovery.id})">
                    Schedule
                  </button>`
                );
              }


              return `

                <tr>

                  <td>
                    #${recovery.id}
                  </td>

                  <td>
                    #${recovery.paymentId}
                  </td>

                  <td>
                    ${App.statusBadge(
                      recovery.status
                    )}
                  </td>

                  <td>
                    ${App.actionBadge(
                      recovery.recommendedAction
                    )}
                  </td>

                  <td>

                    <div class="confidence">

                      <div class="progress">
                        <span style="width:${confidence}%"></span>
                      </div>

                      <span>
                        ${confidence}%
                      </span>

                    </div>

                  </td>

                  <td>
                    ${recovery.attemptCount ?? 0}
                  </td>

                  <td>
                    ${App.esc(
                      recovery.reason ||
                      "Waiting for analysis"
                    )}
                  </td>

                  <td>

                    <div
                      style="
                        display:flex;
                        gap:6px;
                        flex-wrap:wrap;
                      ">

                      ${
                        actions.join("")
                        ||
                        '<span class="muted small">No action</span>'
                      }

                    </div>

                  </td>

                </tr>

              `;
            }
          )
          .join("")
      : `

        <tr>

          <td
            colspan="8"
            class="table-empty">

            No cases match this filter.

          </td>

        </tr>

      `;
}



async function analyzeCase(id) {

  try {

    const response =
      await App.api(
        `/api/v1/recovery-cases/${id}/analyze`,
        {
          method:
            "POST"
        }
      );


    App.toast(
      `Case #${id} analyzed: ${response.recommendedAction}`
    );


    loadCases(
      currentPage
    );


  } catch (error) {

    App.toast(
      error.message,
      "error"
    );
  }
}



async function executeCase(id) {

  App.modal({

    title:
      `Execute Recovery Case #${id}`,

    bodyHtml:
      `
      <div class="notice">
        This will execute the currently planned recovery action
        and advance the recovery state.
      </div>
      `,

    confirmText:
      "Execute",

    confirmClass:
      "btn-success",

    onConfirm:
      async () => {

        const response =
          await App.api(
            `/api/v1/recovery-cases/${id}/execute`,
            {
              method:
                "POST"
            }
          );


        App.toast(
          response.message ||
          `Case #${id} executed`
        );


        loadCases(
          currentPage
        );
      }
  });
}



function scheduleCase(id) {

  const future =
    new Date(
      Date.now() +
      10 * 60 * 1000
    );


  const local =
    new Date(
      future.getTime() -
      future.getTimezoneOffset() *
      60000
    )
      .toISOString()
      .slice(
        0,
        16
      );


  App.modal({

    title:
      `Schedule Recovery Case #${id}`,

    bodyHtml:
      `
      <div class="field">

        <label>
          Retry Date & Time
        </label>

        <input
          id="modalRetryAt"
          type="datetime-local"
          value="${local}"
        />

      </div>

      <div class="notice mt-18">

        Choose a future time.
        The scheduler will process the case automatically when it becomes due.

      </div>
      `,

    confirmText:
      "Schedule Retry",

    onConfirm:
      async modal => {

        const retryAt =
          modal
            .querySelector(
              "#modalRetryAt"
            )
            .value;


        if (!retryAt) {

          throw new Error(
            "Choose a retry time."
          );
        }


        await App.api(
          `/api/v1/recovery-cases/${id}/schedule`,
          {
            method:
              "POST",

            body:
              JSON.stringify(
                {
                  retryAt
                }
              )
          }
        );


        App.toast(
          `Case #${id} scheduled.`
        );


        loadCases(
          currentPage
        );
      }
  });
}



async function processDue() {

  App.setBusy(
    processDueBtn,
    true,
    "Processing..."
  );


  try {

    const response =
      await App.api(
        "/api/v1/recovery-cases/process-due",
        {
          method:
            "POST"
        }
      );


    App.toast(
      `${Array.isArray(response) ? response.length : 0} due retries processed.`
    );


    loadCases(
      currentPage
    );


  } catch (error) {

    App.toast(
      error.message,
      "error"
    );


  } finally {

    App.setBusy(
      processDueBtn,
      false
    );
  }
}
