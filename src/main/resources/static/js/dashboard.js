document.addEventListener(
    "DOMContentLoaded",
    async () => {

        runFastSimulationBtn.onclick =
            () => runSimulation(
                "FAST_SIMULATION",
                100
            );


        runLiveAiSimulationBtn.onclick =
            () => runSimulation(
                "LIVE_AI",
                1
            );


        await Promise.all([
            loadSummary(),
            loadPayments(),
            loadRecoveries()
        ]);
    }
);



// ============================================================
// REAL DASHBOARD SUMMARY
// ============================================================

async function loadSummary() {

    try {

        const summary =
            await App.api(
                "/api/v1/dashboard/summary"
            );


        mTotalOrders.textContent =
            summary.totalOrders ?? 0;


        mFailedPayments.textContent =
            summary.failedPayments ?? 0;


        mRecoveryCases.textContent =
            summary.totalRecoveryCases ?? 0;


        mRecovered.textContent =
            summary.recoveredCases ?? 0;


        pPending.textContent =
            summary.pendingAnalysis ?? 0;


        pPlanned.textContent =
            summary.recoveryPlanned ?? 0;


        pScheduled.textContent =
            summary.scheduledRecoveries ?? 0;


        pHuman.textContent =
            summary.humanReviewCases ?? 0;


        const total =
            Number(
                summary.totalRecoveryCases || 0
            );


        const recovered =
            Number(
                summary.recoveredCases || 0
            );


        const rate =
            total
                ? Math.round(
                    (recovered / total) * 100
                )
                : 0;


        recoveryRate.textContent =
            `${rate}%`;


        recoveryRateBar.style.width =
            `${rate}%`;


        recoveryRateText.textContent =
            total
                ? `${recovered} of ${total} cases`
                : "No cases yet";


    } catch (error) {

        App.toast(
            `Dashboard summary failed: ${error.message}`,
            "error"
        );
    }
}



// ============================================================
// PHASE 9 BUILDATHON SIMULATION
// ============================================================

async function runSimulation(
    mode,
    size
) {

    const button =
        mode === "LIVE_AI"
            ? runLiveAiSimulationBtn
            : runFastSimulationBtn;


    App.setBusy(
        button,
        true,
        mode === "LIVE_AI"
            ? "Calling Gemini..."
            : "Simulating..."
    );


    const otherButton =
        mode === "LIVE_AI"
            ? runFastSimulationBtn
            : runLiveAiSimulationBtn;


    otherButton.disabled =
        true;


    try {

        const response =
            await App.api(
                "/api/v1/buildathon/simulate",
                {
                    method:
                        "POST",

                    body:
                        JSON.stringify(
                            {
                                size:
                                    size,

                                seed:
                                    42,

                                mode:
                                    mode
                            }
                        )
                }
            );


        renderSimulation(
            response
        );


        if (
            mode === "LIVE_AI"
            &&
            Number(
                response.aiDecisions || 0
            ) === 0
            &&
            Number(
                response.ruleFallbacks || 0
            ) > 0
        ) {

            App.toast(
                "Gemini unavailable or quota-limited. Rule fallback handled the case.",
                "error"
            );


        } else {

            App.toast(
                mode === "LIVE_AI"
                    ? "Live AI simulation completed."
                    : "100-case Buildathon simulation completed."
            );
        }


    } catch (error) {

        simulationModeBadge.className =
            "status danger";


        simulationModeBadge.textContent =
            "Simulation failed";


        App.toast(
            error.message,
            "error"
        );


    } finally {

        App.setBusy(
            button,
            false
        );


        otherButton.disabled =
            false;
    }
}



// ============================================================
// RENDER SIMULATION
// ============================================================

function renderSimulation(
    response
) {

    const mode =
        response.mode ||
        "UNKNOWN";


    const batchSize =
        Number(
            response.batchSize || 0
        );


    const recoveryRate =
        Number(
            response.recoveryRate || 0
        );


    const aiDecisions =
        Number(
            response.aiDecisions || 0
        );


    const ruleFallbacks =
        Number(
            response.ruleFallbacks || 0
        );


    simulationModeBadge.className =
        mode === "LIVE_AI"
            ? (
                aiDecisions > 0
                    ? "status success"
                    : "status warning"
            )
            : "status info";


    simulationModeBadge.textContent =
        mode === "LIVE_AI"
            ? (
                aiDecisions > 0
                    ? "LIVE_AI • Gemini used"
                    : "LIVE_AI • Rule fallback"
            )
            : "FAST_SIMULATION";


    simRevenueAtRisk.textContent =
        App.fmtMoney(
            response.revenueAtRisk,
            "INR"
        );


    simRevenueRecovered.textContent =
        App.fmtMoney(
            response.revenueRecovered,
            "INR"
        );


    simRecoveryRate.textContent =
        `${recoveryRate.toFixed(2)}%`;


    simRecoveredCases.textContent =
        response.recoveredCases ?? 0;


    simBatchSizeText.textContent =
        `${response.recoveredCases ?? 0} of ${batchSize} synthetic cases`;


    simAiDecisions.textContent =
        aiDecisions;


    simRuleFallbacks.textContent =
        ruleFallbacks;


    simHumanReviews.textContent =
        response.humanReviews ?? 0;


    simRetriesStopped.textContent =
        response.retriesStopped ?? 0;


    simulationRecoveryBar.style.width =
        `${Math.min(
            100,
            Math.max(
                0,
                recoveryRate
            )
        )}%`;


    simulationSeed.textContent =
        `Seed: ${response.seed ?? "—"}`;


    simulationSummary.textContent =
        `${mode} • ${batchSize} synthetic failed payments • `
        +
        `${response.recoveredCases ?? 0} simulated recoveries • `
        +
        `${aiDecisions} AI decisions • `
        +
        `${ruleFallbacks} rule decisions`;


    renderSimulationCases(
        response.sampleCases || []
    );
}



// ============================================================
// RENDER SIMULATION CASES
// ============================================================

function renderSimulationCases(
    cases
) {

    if (!cases.length) {

        simulationCasesBody.innerHTML = `

            <tr>

                <td
                    colspan="9"
                    class="table-empty">

                    No sample cases returned.

                </td>

            </tr>

        `;

        return;
    }


    simulationCasesBody.innerHTML =
        cases
            .map(
                item => {

                    const confidence =
                        Number(
                            item.confidence || 0
                        );


                    const percentage =
                        Math.round(
                            confidence * 100
                        );


                    const sourceBadge =
                        item.analysisSource === "AI"
                            ? `

                                <span class="status success">

                                    AI

                                </span>

                            `
                            : `

                                <span class="status warning">

                                    RULE FALLBACK

                                </span>

                            `;


                    const outcomeBadge =
                        item.recovered
                            ? `

                                <span class="status success">

                                    RECOVERED

                                </span>

                            `
                            : (
                                item.retryStopped
                                    ? `

                                        <span class="status danger">

                                            RETRY STOPPED

                                        </span>

                                    `
                                    : `

                                        <span class="status neutral">

                                            NOT RECOVERED

                                        </span>

                                    `
                            );


                    return `

                        <tr>


                            <td>

                                #${item.caseNumber}

                            </td>


                            <td>

                                ${App.fmtMoney(
                                    item.amount,
                                    "INR"
                                )}

                            </td>


                            <td>

                                ${App.esc(
                                    item.paymentMethod ||
                                    "—"
                                )}

                            </td>


                            <td>

                                ${App.esc(
                                    item.failureReason ||
                                    "—"
                                )}

                            </td>


                            <td>

                                ${item.previousAttempts ?? 0}

                            </td>


                            <td>

                                ${App.actionBadge(
                                    item.recommendedAction
                                )}

                            </td>


                            <td>

                                <div class="confidence">


                                    <div class="progress">

                                        <span
                                            style="width:${percentage}%">
                                        </span>

                                    </div>


                                    <span>

                                        ${percentage}%

                                    </span>


                                </div>

                            </td>


                            <td>

                                ${sourceBadge}

                            </td>


                            <td>

                                ${outcomeBadge}

                            </td>


                        </tr>

                    `;
                }
            )
            .join("");
}



// ============================================================
// RECENT PAYMENTS
// ============================================================

async function loadPayments() {

    try {

        const page =
            await App.api(
                "/api/v1/payments?page=0&size=6"
            );


        const rows =
            page.content || [];


        recentPayments.innerHTML =
            rows.length
                ? rows
                    .map(
                        payment => `

                            <tr>


                                <td>

                                    #${payment.id}

                                </td>


                                <td class="mono">

                                    ${App.esc(
                                        payment.razorpayPaymentId || "—"
                                    )}

                                </td>


                                <td>

                                    ${App.esc(
                                        payment.paymentMethod || "—"
                                    )}

                                </td>


                                <td>

                                    ${App.fmtMoney(
                                        payment.amount,
                                        payment.currency
                                    )}

                                </td>


                                <td>

                                    ${App.statusBadge(
                                        payment.status
                                    )}

                                </td>


                                <td>

                                    ${App.esc(
                                        payment.failureReason || "—"
                                    )}

                                </td>


                            </tr>

                        `
                    )
                    .join("")
                : `

                    <tr>

                        <td
                            colspan="6"
                            class="table-empty">

                            No payments found.

                        </td>

                    </tr>

                `;


    } catch {

        recentPayments.innerHTML = `

            <tr>

                <td
                    colspan="6"
                    class="table-empty">

                    Unable to load payments.

                </td>

            </tr>

        `;
    }
}



// ============================================================
// RECENT RECOVERY CASES
// ============================================================

async function loadRecoveries() {

    try {

        const page =
            await App.api(
                "/api/v1/recovery-cases?page=0&size=6"
            );


        const rows =
            page.content || [];


        recentRecoveries.innerHTML =
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

                                                <span
                                                    style="width:${confidence}%">
                                                </span>

                                            </div>


                                            <span>

                                                ${confidence}%

                                            </span>


                                        </div>

                                    </td>


                                    <td>

                                        ${App.esc(
                                            recovery.reason
                                            ||
                                            "Waiting for analysis"
                                        )}

                                    </td>


                                </tr>

                            `;
                        }
                    )
                    .join("")
                : `

                    <tr>

                        <td
                            colspan="6"
                            class="table-empty">

                            No recovery cases found.

                        </td>

                    </tr>

                `;


    } catch {

        recentRecoveries.innerHTML = `

            <tr>

                <td
                    colspan="6"
                    class="table-empty">

                    Unable to load recovery cases.

                </td>

            </tr>

        `;
    }
}