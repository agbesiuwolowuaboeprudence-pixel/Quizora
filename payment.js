(function () {
    const payButton = document.getElementById("payButton");

    if (!payButton) {
        return;
    }

    const paymentMessage = document.getElementById("paymentMessage");
    const planCards = document.querySelectorAll(".plan-card");
    const summaryPlan = document.getElementById("summaryPlan");
    const summaryPeriod = document.getElementById("summaryPeriod");
    const summaryMethod = document.getElementById("summaryMethod");
    const summaryStatus = document.getElementById("summaryAmount");
    let selectedPlan = "MONTHLY";

    function showMessage(text, type) {
        paymentMessage.textContent = text;
        paymentMessage.className = `payment-message ${type || "error"}`;
    }

    function selectPlan(card) {
        planCards.forEach(item => {
            const selected = item === card;
            item.classList.toggle("selected", selected);
            item.setAttribute("aria-pressed", String(selected));
            item.querySelector(".plan-radio").checked = selected;
        });

        selectedPlan = card.dataset.plan;
        summaryPlan.textContent = selectedPlan === "MONTHLY"
            ? "Monthly Plan"
            : "Yearly Plan";
        summaryPeriod.textContent = selectedPlan === "MONTHLY"
            ? "1 Month"
            : "12 Months";
        paymentMessage.textContent = "";
        paymentMessage.className = "payment-message";
    }

    planCards.forEach(card => {
        card.addEventListener("click", () => selectPlan(card));
        card.addEventListener("keydown", event => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                selectPlan(card);
            }
        });
    });

    summaryMethod.textContent = "Mock activation";

    payButton.addEventListener("click", async () => {
        if (!QuizoraApi.isAuthenticated()) {
            showMessage("Please log in before activating a subscription.");
            return;
        }

        payButton.disabled = true;
        payButton.textContent = "Activating…";
        paymentMessage.textContent = "";

        try {
            const subscription = await QuizoraApi.subscribe({
                plan: selectedPlan
            });

            if (
                !subscription ||
                subscription.plan !== selectedPlan ||
                typeof subscription.status !== "string" ||
                typeof subscription.endDate !== "string"
            ) {
                throw new Error(
                    "The subscription response did not match the documented API contract."
                );
            }

            summaryStatus.textContent =
                `${subscription.status} through ${subscription.endDate}`;
            showMessage(
                `${subscription.plan} subscription activated. No payment was collected.`,
                "success"
            );
            payButton.textContent = "Returning to Dashboard…";

            window.setTimeout(() => {
                window.location.assign("Dashboard.html");
            }, 1200);
        } catch (error) {
            showMessage(
                error.message ||
                    "The subscription could not be activated."
            );
            payButton.disabled = false;
            payButton.textContent = "Activate Subscription →";
        }
    });
})();
