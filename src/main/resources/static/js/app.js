const App = (() => {

    async function api(
        path,
        options = {}
    ) {

        const config = {

            headers: {

                "Content-Type":
                    "application/json",

                ...(options.headers || {})
            },

            ...options
        };


        const response =
            await fetch(
                path,
                config
            );


        const type =
            response.headers.get(
                "content-type"
            ) || "";


        const body =
            type.includes(
                "application/json"
            )
                ? await response.json()
                : await response.text();


        if (!response.ok) {

            const detail =
                typeof body === "string"
                    ? body
                    : (
                        body?.message
                        ||
                        body?.error
                        ||
                        JSON.stringify(
                            body
                        )
                    );


            throw new Error(

                `${response.status} ${response.statusText}`

                +

                (
                    detail
                        ? ` — ${detail}`
                        : ""
                )
            );
        }


        return body;
    }



    function esc(
        value = ""
    ) {

        return String(
            value
        )
            .replaceAll(
                "&",
                "&amp;"
            )
            .replaceAll(
                "<",
                "&lt;"
            )
            .replaceAll(
                ">",
                "&gt;"
            )
            .replaceAll(
                '"',
                "&quot;"
            )
            .replaceAll(
                "'",
                "&#039;"
            );
    }



    function fmtMoney(
        paise,
        currency = "INR"
    ) {

        if (
            paise == null
        ) {

            return "—";
        }


        try {

            return new Intl.NumberFormat(
                "en-IN",
                {
                    style:
                        "currency",

                    currency:
                        currency
                }
            ).format(
                Number(
                    paise
                ) / 100
            );


        } catch {

            return `₹${(
                Number(
                    paise
                ) / 100
            ).toFixed(2)}`;
        }
    }



    function fmtDate(
        value
    ) {

        if (!value) {

            return "—";
        }


        const date =
            new Date(
                value
            );


        return Number.isNaN(
            date.getTime()
        )
            ? value
            : date.toLocaleString(
                "en-IN"
            );
    }



    function statusClass(
        status = ""
    ) {

        const value =
            String(
                status
            ).toUpperCase();


        if (
            [
                "PAID",
                "CAPTURED",
                "RECOVERED",
                "PROCESSED",
                "AUTHORIZED"
            ].includes(
                value
            )
        ) {

            return "success";
        }


        if (
            [
                "FAILED",
                "CANCELLED"
            ].includes(
                value
            )
        ) {

            return "danger";
        }


        if (
            [
                "PENDING_ANALYSIS",
                "ANALYZING",
                "RECOVERY_SCHEDULED",
                "PENDING"
            ].includes(
                value
            )
        ) {

            return "warning";
        }


        if (
            [
                "RECOVERY_PLANNED",
                "RECOVERY_IN_PROGRESS",
                "ATTEMPTED",
                "CREATED"
            ].includes(
                value
            )
        ) {

            return "info";
        }


        return "neutral";
    }



    function statusBadge(
        status
    ) {

        return `

            <span
                class="status ${statusClass(status)}">

                ${esc(
                    status || "UNKNOWN"
                )}

            </span>

        `;
    }



    function actionBadge(
        action
    ) {

        if (!action) {

            return `

                <span class="status neutral">
                    NOT DECIDED
                </span>

            `;
        }


        return `

            <span class="status info">

                ${esc(
                    action
                )}

            </span>

        `;
    }



    function toast(
        message,
        type = "success"
    ) {

        let box =
            document.querySelector(
                ".toast-container"
            );


        if (!box) {

            box =
                document.createElement(
                    "div"
                );


            box.className =
                "toast-container";


            document.body.appendChild(
                box
            );
        }


        const toast =
            document.createElement(
                "div"
            );


        toast.className =
            `toast ${type}`;


        toast.textContent =
            message;


        box.appendChild(
            toast
        );


        setTimeout(
            () => toast.remove(),
            3500
        );
    }



    function setBusy(
        button,
        busy,
        busyText = "Working..."
    ) {

        if (!button) {

            return;
        }


        if (busy) {

            button.dataset.oldText =
                button.innerHTML;


            button.innerHTML =
                busyText;


            button.disabled =
                true;


        } else {

            button.innerHTML =
                button.dataset.oldText
                ||
                button.innerHTML;


            button.disabled =
                false;
        }
    }



    function pageMeta(
        page
    ) {

        return {

            number:
                page?.number ?? 0,

            totalPages:
                page?.totalPages ?? 0,

            totalElements:
                page?.totalElements ?? 0,

            first:
                page?.first ?? true,

            last:
                page?.last ?? true
        };
    }



    function pager(
        meta,
        onPage
    ) {

        const wrap =
            document.createElement(
                "div"
            );


        wrap.className =
            "pagination";


        wrap.innerHTML = `

            <div class="page-info">

                ${meta.totalElements} records •

                Page
                ${meta.totalPages ? meta.number + 1 : 0}

                of
                ${meta.totalPages}

            </div>


            <button
                class="btn btn-secondary btn-sm"
                ${meta.first ? "disabled" : ""}>

                Previous

            </button>


            <button
                class="btn btn-secondary btn-sm"
                ${meta.last ? "disabled" : ""}>

                Next

            </button>

        `;


        const [
            previous,
            next
        ] =
            wrap.querySelectorAll(
                "button"
            );


        previous.onclick =
            () =>
                onPage(
                    meta.number - 1
                );


        next.onclick =
            () =>
                onPage(
                    meta.number + 1
                );


        return wrap;
    }



    async function checkAI() {

        const dots =
            document.querySelectorAll(
                "[data-ai-dot]"
            );


        const labels =
            document.querySelectorAll(
                "[data-ai-label]"
            );


        try {

            const result =
                await api(
                    "/api/v1/ai/test"
                );


            if (
                typeof result === "object"
                &&
                result?.status === "DEGRADED"
            ) {

                dots.forEach(
                    dot =>
                        dot.className =
                            "dot warning"
                );


                labels.forEach(
                    label =>
                        label.textContent =
                            "Rule fallback active"
                );


                return;
            }


            dots.forEach(
                dot =>
                    dot.className =
                        "dot success"
            );


            labels.forEach(
                label =>
                    label.textContent =
                        "Gemini connected"
            );


        } catch {

            dots.forEach(
                dot =>
                    dot.className =
                        "dot danger"
            );


            labels.forEach(
                label =>
                    label.textContent =
                        "AI status unavailable"
            );
        }
    }



    function initShell() {

        const menu =
            document.querySelector(
                "[data-mobile-menu]"
            );


        const sidebar =
            document.querySelector(
                ".sidebar"
            );


        if (
            menu
            &&
            sidebar
        ) {

            menu.onclick =
                () =>
                    sidebar.classList.toggle(
                        "open"
                    );
        }


        document.addEventListener(
            "click",
            event => {

                if (
                    window.innerWidth <= 820
                    &&
                    sidebar?.classList.contains(
                        "open"
                    )
                    &&
                    !sidebar.contains(
                        event.target
                    )
                    &&
                    event.target !== menu
                ) {

                    sidebar.classList.remove(
                        "open"
                    );
                }
            }
        );


        checkAI();
    }



    function modal({

        title,

        bodyHtml,

        confirmText = "Confirm",

        confirmClass = "btn-primary",

        onConfirm

    }) {

        const backdrop =
            document.createElement(
                "div"
            );


        backdrop.className =
            "modal-backdrop";


        backdrop.innerHTML = `

            <div class="modal">


                <div class="modal-head">

                    <div class="card-title">

                        ${esc(
                            title
                        )}

                    </div>


                    <button class="modal-close">

                        ✕

                    </button>

                </div>


                <div class="modal-body">

                    ${bodyHtml}

                </div>


                <div class="modal-actions">

                    <button
                        class="btn btn-secondary"
                        data-cancel>

                        Cancel

                    </button>


                    <button
                        class="btn ${confirmClass}"
                        data-confirm>

                        ${esc(
                            confirmText
                        )}

                    </button>

                </div>


            </div>

        `;


        document.body.appendChild(
            backdrop
        );


        const close =
            () =>
                backdrop.remove();


        backdrop
            .querySelector(
                ".modal-close"
            )
            .onclick =
            close;


        backdrop
            .querySelector(
                "[data-cancel]"
            )
            .onclick =
            close;


        backdrop.onclick =
            event => {

                if (
                    event.target === backdrop
                ) {

                    close();
                }
            };


        backdrop
            .querySelector(
                "[data-confirm]"
            )
            .onclick =
            async event => {

                try {

                    setBusy(
                        event.currentTarget,
                        true,
                        "Working..."
                    );


                    await onConfirm?.(
                        backdrop
                    );


                    close();


                } catch (error) {

                    toast(
                        error.message,
                        "error"
                    );


                    setBusy(
                        event.currentTarget,
                        false
                    );
                }
            };


        return backdrop;
    }



    return {

        api,

        esc,

        fmtMoney,

        fmtDate,

        statusBadge,

        actionBadge,

        toast,

        setBusy,

        pageMeta,

        pager,

        checkAI,

        initShell,

        modal
    };
})();



document.addEventListener(
    "DOMContentLoaded",
    App.initShell
);