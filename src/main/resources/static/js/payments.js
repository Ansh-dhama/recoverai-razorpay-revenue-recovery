let ordersPage = 0;

let paymentsPage = 0;

let latestOrderId = "";

let razorpayPublicKey = "";

let latestAmountPaise = 0;

let latestCustomerId = "";



document.addEventListener(
  "DOMContentLoaded",
  async () => {

    merchantOrderId.value =
      `RECOVER-AI-${Date.now()}`;


    receipt.value =
      `receipt_${Date.now()}`;


    createOrderForm.addEventListener(
      "submit",
      createOrder
    );


    checkoutBtn.onclick =
      openCheckout;


    refreshOrders.onclick =
      () => loadOrders(ordersPage);


    refreshPayments.onclick =
      () => loadPayments(paymentsPage);


    await loadRazorpayConfig();


    loadOrders(0);

    loadPayments(0);
  }
);



async function loadRazorpayConfig() {

  try {

    const config =
      await App.api(
        "/api/v1/config/razorpay"
      );


    razorpayPublicKey =
      config.keyId || "";


    if (!razorpayPublicKey) {

      throw new Error(
        "Razorpay Key ID was not returned."
      );
    }


    razorpayStatus.className =
      "status success";


    razorpayStatus.textContent =
      "Razorpay configured";


  } catch (error) {

    razorpayPublicKey = "";


    razorpayStatus.className =
      "status danger";


    razorpayStatus.textContent =
      "Razorpay unavailable";


    App.toast(
      "Unable to load Razorpay configuration.",
      "error"
    );
  }
}



async function createOrder(event) {

  event.preventDefault();


  const amountPaise =
    Math.round(
      Number(
        amountRupees.value
      ) * 100
    );


  if (!amountPaise || amountPaise < 100) {

    App.toast(
      "Enter a valid amount.",
      "error"
    );

    return;
  }


  const body = {

    merchantOrderId:
      merchantOrderId.value.trim(),

    customerId:
      customerId.value.trim(),

    amount:
      amountPaise,

    currency:
      "INR",

    receipt:
      receipt.value.trim()
  };


  App.setBusy(
    createBtn,
    true,
    "Creating..."
  );


  try {

    const response =
      await App.api(
        "/api/v1/payment-orders",
        {
          method:
            "POST",

          body:
            JSON.stringify(body)
        }
      );


    const displayResponse = {

      id:
        response.id,

      merchantOrderId:
        response.merchantOrderId,

      razorpayOrderId:
        response.razorpayOrderId,

      customerId:
        response.customerId,

      amount:
        response.amount,

      currency:
        response.currency,

      receipt:
        response.receipt,

      status:
        response.status,

      attempts:
        response.attempts,

      createdAt:
        response.createdAt
    };


    createResult.textContent =
      JSON.stringify(
        displayResponse,
        null,
        2
      );


    latestOrderId =
      response.razorpayOrderId || "";


    latestAmountPaise =
      response.amount || amountPaise;


    latestCustomerId =
      response.customerId ||
      body.customerId;


    if (!latestOrderId) {

      throw new Error(
        "Backend did not return Razorpay order ID."
      );
    }


    checkoutBtn.disabled =
      !razorpayPublicKey;


    App.toast(
      "Razorpay order created."
    );


    loadOrders(0);


  } catch (error) {

    createResult.textContent =
      error.message;


    App.toast(
      error.message,
      "error"
    );


  } finally {

    App.setBusy(
      createBtn,
      false
    );
  }
}



function openCheckout() {

  if (!razorpayPublicKey) {

    App.toast(
      "Razorpay configuration is unavailable.",
      "error"
    );

    return;
  }


  if (!latestOrderId) {

    App.toast(
      "Create a new payment order first.",
      "error"
    );

    return;
  }


  if (typeof Razorpay === "undefined") {

    App.toast(
      "Razorpay Checkout library failed to load.",
      "error"
    );

    return;
  }


  checkoutResult.textContent =
    "Opening Razorpay Checkout...";


  const options = {

    key:
      razorpayPublicKey,

    order_id:
      latestOrderId,

    name:
      "RecoverAI",

    description:
      "Failed Payment Recovery Demo",

    prefill: {
      name:
        latestCustomerId ||
        "RecoverAI Customer"
    },

    handler:
      function (response) {

        checkoutResult.textContent =
          JSON.stringify(
            {
              message:
                "Checkout reported success. Waiting for Razorpay webhook confirmation.",

              paymentId:
                response.razorpay_payment_id,

              orderId:
                response.razorpay_order_id
            },
            null,
            2
          );


        App.toast(
          "Payment successful. Waiting for webhook."
        );


        setTimeout(
          () => {

            loadOrders(0);

            loadPayments(0);

          },
          2500
        );
      },

    modal: {

      ondismiss:
        function () {

          checkoutResult.textContent =
            "Checkout closed.";

        }
    }
  };


  const checkout =
    new Razorpay(
      options
    );


  checkout.on(
    "payment.failed",
    function (response) {

      const error =
        response.error || {};


      checkoutResult.textContent =
        JSON.stringify(
          {
            message:
              "Payment failed. Waiting for payment.failed webhook.",

            code:
              error.code,

            description:
              error.description,

            source:
              error.source,

            step:
              error.step,

            reason:
              error.reason
          },
          null,
          2
        );


      App.toast(
        "Payment failed — recovery flow started.",
        "error"
      );


      setTimeout(
        () => {

          loadPayments(0);

        },
        2500
      );
    }
  );


  checkout.open();
}



async function loadOrders(page) {

  ordersPage =
    page;


  try {

    const response =
      await App.api(
        `/api/v1/payment-orders?page=${page}&size=8`
      );


    const rows =
      response.content || [];


    ordersBody.innerHTML =
      rows.length
        ? rows
            .map(
              order => `

                <tr>

                  <td>
                    #${order.id}
                  </td>

                  <td>
                    ${App.esc(
                      order.merchantOrderId
                    )}
                  </td>

                  <td class="mono">
                    ${App.esc(
                      order.razorpayOrderId || "—"
                    )}
                  </td>

                  <td>
                    ${App.esc(
                      order.customerId
                    )}
                  </td>

                  <td>
                    ${App.fmtMoney(
                      order.amount,
                      order.currency
                    )}
                  </td>

                  <td>
                    ${App.statusBadge(
                      order.status
                    )}
                  </td>

                  <td>
                    ${order.attempts ?? 0}
                  </td>

                </tr>

              `
            )
            .join("")
        : `

          <tr>

            <td
              colspan="7"
              class="table-empty">

              No payment orders found.

            </td>

          </tr>

        `;


    ordersPager.replaceChildren(

      App.pager(

        App.pageMeta(response),

        loadOrders
      )
    );


  } catch (error) {

    ordersBody.innerHTML = `

      <tr>

        <td
          colspan="7"
          class="table-empty">

          Failed to load payment orders.

        </td>

      </tr>

    `;


    console.error(
      error
    );
  }
}



async function loadPayments(page) {

  paymentsPage =
    page;


  try {

    const response =
      await App.api(
        `/api/v1/payments?page=${page}&size=8`
      );


    const rows =
      response.content || [];


    paymentsBody.innerHTML =
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

                  <td class="mono">

                    ${App.esc(
                      payment.orderId || "—"
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

                    <div>

                      ${App.esc(
                        payment.failureReason || "—"
                      )}

                    </div>

                    <div class="small muted">

                      ${App.esc(
                        payment.failureDescription || ""
                      )}

                    </div>

                  </td>

                </tr>

              `
            )
            .join("")
        : `

          <tr>

            <td
              colspan="7"
              class="table-empty">

              No payment attempts found.

            </td>

          </tr>

        `;


    paymentsPager.replaceChildren(

      App.pager(

        App.pageMeta(response),

        loadPayments
      )
    );


  } catch (error) {

    paymentsBody.innerHTML = `

      <tr>

        <td
          colspan="7"
          class="table-empty">

          Failed to load payments.

        </td>

      </tr>

    `;


    console.error(
      error
    );
  }
}
