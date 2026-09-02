let currentPage = 0;


document.addEventListener(
  "DOMContentLoaded",
  () => {

    localWebhook.textContent =
      `${location.origin}/api/v1/webhooks/razorpay`;


    refreshEvents.onclick =
      () => loadEvents(currentPage);


    loadEvents(0);
  }
);



async function loadEvents(page) {

  currentPage =
    page;


  try {

    const response =
      await App.api(
        `/api/v1/webhook-events?page=${page}&size=12`
      );


    const rows =
      response.content || [];


    eventsBody.innerHTML =
      rows.length
        ? rows
            .map(
              event => `

                <tr>

                  <td class="mono">
                    ${App.esc(
                      event.eventId
                    )}
                  </td>

                  <td>
                    ${App.esc(
                      event.eventType
                    )}
                  </td>

                  <td>
                    ${App.statusBadge(
                      event.status
                    )}
                  </td>

                  <td>
                    ${App.fmtDate(
                      event.receivedAt
                    )}
                  </td>

                  <td>
                    ${App.fmtDate(
                      event.processedAt
                    )}
                  </td>

                </tr>

              `
            )
            .join("")
        : `

          <tr>

            <td
              colspan="5"
              class="table-empty">

              No webhook events found.

            </td>

          </tr>

        `;


    eventsPager.replaceChildren(

      App.pager(

        App.pageMeta(response),

        loadEvents
      )
    );


  } catch (error) {

    eventsBody.innerHTML = `

      <tr>

        <td
          colspan="5"
          class="table-empty">

          Unable to load webhook events.

        </td>

      </tr>

    `;


    App.toast(
      error.message,
      "error"
    );
  }
}
