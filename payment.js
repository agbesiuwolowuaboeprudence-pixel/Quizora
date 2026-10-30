
const payButton = document.getElementById("payButton");
const paymentMessage = document.getElementById("paymentMessage");

payButton.addEventListener("click", async () => {
    const selectedCard = document.querySelector(".plan-card.selected");
    const plan = selectedCard?.dataset.plan;
    const method = document.querySelector(
        'input[name="paymentMethod"]:checked'
    )?.value;

    if (!plan || !method) {
        paymentMessage.textContent = "Please select a plan and payment method.";
        return;
    }

    const token = localStorage.getItem("quizoraToken");

    if (!token) {
        paymentMessage.textContent =
            "Please log in before making a payment.";
        return;
    }

    payButton.disabled = true;
    payButton.textContent = "Preparing checkout...";
    paymentMessage.textContent = "";

    try {
        // Your teammate will confirm this endpoint and response.
        const response = await fetch(
            "http://localhost:8080/api/payments/initialize",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({
                    plan: plan,
                    paymentMethod: method
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Unable to start payment.");
        }

        // Redirect only to the checkout URL supplied by the backend.
        const checkoutUrl = data.authorizationUrl;

        if (!checkoutUrl) {
            throw new Error("Payment checkout URL was not returned.");
        }

        const url = new URL(checkoutUrl);

        if (
            url.protocol !== "https:" ||
            !(
                url.hostname === "paystack.com" ||
                url.hostname.endsWith(".paystack.com")
            )
        ) {
            throw new Error("Unexpected payment checkout URL.");
        }

        window.location.assign(url.href);

    } catch (error) {
        paymentMessage.textContent = error.message;
        payButton.disabled = false;
        payButton.textContent = "Proceed to Payment";
    }
});
