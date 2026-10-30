document.addEventListener("DOMContentLoaded", () => {
    const studentName =
        localStorage.getItem("quizoraStudentName") || "Student";

    const isLoggedIn =
        Boolean(localStorage.getItem("quizoraStudentName"));

    document.querySelectorAll("[data-student-name]").forEach(element => {
        element.textContent = studentName;
    });

    setupMobileMenu();
    setupHomePage(isLoggedIn);
    setupLogout();
    setupLogin();
    setupRegistration();
    setupDashboard();
    setupAdminDashboard();
    setupCourses();
    setupQuiz();
    setupResults();
});


/* =========================================================
   MOBILE MENU
========================================================= */

function setupMobileMenu() {
    const menuButton = document.getElementById("menuButton");
    const mobileMenu = document.getElementById("mobileMenu");

    if (!menuButton || !mobileMenu) {
        return;
    }

    menuButton.addEventListener("click", () => {
        mobileMenu.classList.toggle("open");
    });

    mobileMenu.querySelectorAll("a").forEach(link => {
        link.addEventListener("click", () => {
            mobileMenu.classList.remove("open");
        });
    });
}


/* =========================================================
   HOME PAGE
========================================================= */

function setupHomePage(isLoggedIn) {
    const guestActions =
        document.getElementById("guestActions");

    const studentActions =
        document.getElementById("studentActions");

    const mobileLoginLink =
        document.getElementById("mobileLoginLink");

    const mobileLogoutButton =
        document.getElementById("mobileLogoutButton");

    const heroStartButton =
        document.getElementById("heroStartButton");

    const ctaStartButton =
        document.getElementById("ctaStartButton");

    if (guestActions && studentActions) {
        if (isLoggedIn) {
            guestActions.style.display = "none";
            studentActions.style.display = "flex";
        } else {
            guestActions.style.display = "flex";
            studentActions.style.display = "none";
        }
    }

    if (mobileLoginLink && mobileLogoutButton) {
        if (isLoggedIn) {
            mobileLoginLink.style.display = "none";
            mobileLogoutButton.style.display = "block";
        } else {
            mobileLoginLink.style.display = "block";
            mobileLogoutButton.style.display = "none";
        }
    }

    if (isLoggedIn) {
        if (heroStartButton) {
            heroStartButton.href = "dashboard.html";
            heroStartButton.textContent = "Continue Learning →";
        }

        if (ctaStartButton) {
            ctaStartButton.href = "dashboard.html";
            ctaStartButton.textContent = "Continue Learning →";
        }
    }
}


/* =========================================================
   LOGOUT
========================================================= */

function setupLogout() {
    document.querySelectorAll("[data-logout]").forEach(button => {
        button.addEventListener("click", () => {
            localStorage.removeItem("quizoraStudentName");
            localStorage.removeItem("quizoraStudentEmail");
            localStorage.removeItem("quizoraSelection");
            localStorage.removeItem("quizoraLastResult");

            /*
                Quiz history is deliberately kept.
                The backend will handle permanent
                student data later.
            */

            window.location.href = "login.html";
        });
    });
}


/* =========================================================
   LOGIN
========================================================= */

function setupLogin() {
    const loginForm =
        document.getElementById("loginForm");

    if (!loginForm) {
        return;
    }

    const nameInput =
        document.getElementById("studentName");

    const emailInput =
        document.getElementById("studentEmail");

    const passwordInput =
        document.getElementById("studentPassword");

    const passwordToggle =
        document.getElementById("passwordToggle");

    const loginMessage =
        document.getElementById("loginMessage");

    const savedName =
        localStorage.getItem("quizoraStudentName");

    const savedEmail =
        localStorage.getItem("quizoraStudentEmail");

    if (savedName && nameInput) {
        nameInput.value = savedName;
    }

    if (savedEmail && emailInput) {
        emailInput.value = savedEmail;
    }

    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener("click", () => {
            const passwordVisible =
                passwordInput.type === "text";

            passwordInput.type =
                passwordVisible ? "password" : "text";

            passwordToggle.textContent =
                passwordVisible ? "Show" : "Hide";
        });
    }

    loginForm.addEventListener("submit", event => {
        event.preventDefault();

        const name =
            nameInput.value.trim();

        const email =
            emailInput.value.trim();

        const password =
            passwordInput.value.trim();

        if (!name || !email || !password) {
            showLoginMessage(
                loginMessage,
                "Please complete all fields.",
                "error"
            );

            return;
        }

        if (password.length < 4) {
            showLoginMessage(
                loginMessage,
                "Password must contain at least 4 characters.",
                "error"
            );

            return;
        }

        localStorage.setItem(
            "quizoraStudentName",
            name
        );

        localStorage.setItem(
            "quizoraStudentEmail",
            email
        );

        showLoginMessage(
            loginMessage,
            "Login successful. Preparing your session...",
            "success"
        );

        setTimeout(() => {
            window.location.href = "loading.html";
        }, 500);
    });
}


function showLoginMessage(element, message, type) {
    if (!element) {
        return;
    }

    element.textContent = message;
    element.className = `form-message ${type}`;
}


/* =========================================================
   REGISTRATION / ONBOARDING
========================================================= */

function setupRegistration() {
    const stepOne =
        document.getElementById("registerStepOne");

    const stepTwo =
        document.getElementById("registerStepTwo");

    const stepThree =
        document.getElementById("registerStepThree");

    if (!stepOne || !stepTwo || !stepThree) {
        return;
    }

    const accountTypeCards =
        document.querySelectorAll("[data-account-type]");

    const progressItems =
        document.querySelectorAll(".register-progress-item");

    const continueButton =
        document.getElementById("accountTypeContinue");

    const detailsForm =
        document.getElementById("registrationDetailsForm");

    const nameInput =
        document.getElementById("registerName");

    const emailInput =
        document.getElementById("registerEmail");

    const passwordInput =
        document.getElementById("registerPassword");

    const confirmPasswordInput =
        document.getElementById("registerConfirmPassword");

    const passwordToggle =
        document.getElementById("registerPasswordToggle");

    const registerMessage =
        document.getElementById("registerMessage");

    const individualAccess =
        document.getElementById("individualAccess");

    const institutionalAccess =
        document.getElementById("institutionalAccess");

    const institutionCode =
        document.getElementById("institutionCode");

    const institutionMessage =
        document.getElementById("institutionMessage");

    const individualBackButton =
        document.getElementById("individualBackButton");

    const institutionBackButton =
        document.getElementById("institutionBackButton");

    const registerBackOne =
        document.getElementById("registerBackOne");

    const completeIndividualButton =
        document.getElementById(
            "completeIndividualRegistration"
        );

    const completeInstitutionButton =
        document.getElementById(
            "completeInstitutionRegistration"
        );

    const paymentOptions =
        document.querySelectorAll("[data-payment-method]");

    let selectedAccountType = "individual";
    let selectedPaymentMethod = "mobile-money";


    function showStep(stepNumber) {
        stepOne.classList.remove("active");
        stepTwo.classList.remove("active");
        stepThree.classList.remove("active");

        if (stepNumber === 1) {
            stepOne.classList.add("active");
        }

        if (stepNumber === 2) {
            stepTwo.classList.add("active");
        }

        if (stepNumber === 3) {
            stepThree.classList.add("active");
        }

        progressItems.forEach((item, index) => {
            item.classList.toggle(
                "active",
                index < stepNumber
            );
        });

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }


    accountTypeCards.forEach(card => {
        card.addEventListener("click", () => {
            accountTypeCards.forEach(item => {
                item.classList.remove("active");
            });

            card.classList.add("active");

            selectedAccountType =
                card.getAttribute("data-account-type") ||
                "individual";
        });
    });


    if (continueButton) {
        continueButton.addEventListener("click", () => {
            showStep(2);
        });
    }


    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener("click", () => {
            const isVisible =
                passwordInput.type === "text";

            passwordInput.type =
                isVisible ? "password" : "text";

            passwordToggle.textContent =
                isVisible ? "Show" : "Hide";
        });
    }


    if (detailsForm) {
        detailsForm.addEventListener("submit", event => {
            event.preventDefault();

            const name =
                nameInput
                    ? nameInput.value.trim()
                    : "";

            const email =
                emailInput
                    ? emailInput.value.trim()
                    : "";

            const password =
                passwordInput
                    ? passwordInput.value
                    : "";

            const confirmPassword =
                confirmPasswordInput
                    ? confirmPasswordInput.value
                    : "";

            clearRegisterMessage(registerMessage);

            if (
                !name ||
                !email ||
                !password ||
                !confirmPassword
            ) {
                showRegisterMessage(
                    registerMessage,
                    "Please complete all fields.",
                    "error"
                );

                return;
            }

            if (password.length < 4) {
                showRegisterMessage(
                    registerMessage,
                    "Password must contain at least 4 characters.",
                    "error"
                );

                return;
            }

            if (password !== confirmPassword) {
                showRegisterMessage(
                    registerMessage,
                    "The passwords do not match.",
                    "error"
                );

                return;
            }

            localStorage.setItem(
                "quizoraPendingName",
                name
            );

            localStorage.setItem(
                "quizoraPendingEmail",
                email
            );

            localStorage.setItem(
                "quizoraAccountType",
                selectedAccountType
            );

            if (selectedAccountType === "individual") {
                if (individualAccess) {
                    individualAccess.style.display = "block";
                }

                if (institutionalAccess) {
                    institutionalAccess.style.display = "none";
                }
            } else {
                if (individualAccess) {
                    individualAccess.style.display = "none";
                }

                if (institutionalAccess) {
                    institutionalAccess.style.display = "block";
                }
            }

            showStep(3);
        });
    }


    if (registerBackOne) {
        registerBackOne.addEventListener("click", () => {
            showStep(1);
        });
    }


    if (individualBackButton) {
        individualBackButton.addEventListener(
            "click",
            () => {
                showStep(2);
            }
        );
    }


    if (institutionBackButton) {
        institutionBackButton.addEventListener(
            "click",
            () => {
                clearRegisterMessage(
                    institutionMessage
                );

                showStep(2);
            }
        );
    }


    paymentOptions.forEach(option => {
        option.addEventListener("click", () => {
            paymentOptions.forEach(item => {
                item.classList.remove("active");
            });

            option.classList.add("active");

            selectedPaymentMethod =
                option.getAttribute(
                    "data-payment-method"
                ) || "mobile-money";
        });
    });


    if (completeIndividualButton) {
        completeIndividualButton.addEventListener(
            "click",
            () => {
                localStorage.setItem(
                    "quizoraPaymentMethod",
                    selectedPaymentMethod
                );

                completeRegistration();
            }
        );
    }


    if (completeInstitutionButton) {
        completeInstitutionButton.addEventListener(
            "click",
            () => {
                const code =
                    institutionCode
                        ? institutionCode.value.trim()
                        : "";

                clearRegisterMessage(
                    institutionMessage
                );

                if (!code) {
                    showRegisterMessage(
                        institutionMessage,
                        "Please enter your institutional access code.",
                        "error"
                    );

                    return;
                }

                /*
                    FRONTEND DEMO ONLY.

                    Real institutional code verification
                    and seat availability will be handled
                    by the backend.
                */

                localStorage.setItem(
                    "quizoraInstitutionCode",
                    code
                );

                completeRegistration();
            }
        );
    }


    function completeRegistration() {
        const name =
            localStorage.getItem(
                "quizoraPendingName"
            );

        const email =
            localStorage.getItem(
                "quizoraPendingEmail"
            );

        if (!name || !email) {
            showStep(2);
            return;
        }

        localStorage.setItem(
            "quizoraStudentName",
            name
        );

        localStorage.setItem(
            "quizoraStudentEmail",
            email
        );

        localStorage.removeItem(
            "quizoraPendingName"
        );

        localStorage.removeItem(
            "quizoraPendingEmail"
        );

        window.location.href = "loading.html";
    }


    showStep(1);
}


function showRegisterMessage(element, message, type) {
    if (!element) {
        return;
    }

    element.textContent = message;
    element.className =
        `register-message ${type}`;
}


function clearRegisterMessage(element) {
    if (!element) {
        return;
    }

    element.textContent = "";
    element.className = "register-message";
}


/* =========================================================
   DASHBOARD
========================================================= */

function setupDashboard() {
    const attemptsElement =
        document.getElementById("dashboardAttempts");

    if (!attemptsElement) {
        return;
    }

    const studentName =
        localStorage.getItem("quizoraStudentName");

    if (!studentName) {
        window.location.href = "login.html";
        return;
    }

    const history = getQuizHistory();

    const attempts = history.length;

    let averageAccuracy = 0;
    let bestScore = 0;
    let recentSubject = "—";

    if (attempts > 0) {
        const totalAccuracy = history.reduce(
            (total, result) => {
                return total +
                    Number(result.percentage || 0);
            },
            0
        );

        averageAccuracy =
            Math.round(totalAccuracy / attempts);

        bestScore =
            Math.max(
                ...history.map(result =>
                    Number(result.percentage || 0)
                )
            );

        const latestResult =
            history[history.length - 1];

        recentSubject =
            latestResult.subject || "—";

        displayRecentDashboardActivity(
            latestResult
        );
    }

    setText(
        "dashboardAttempts",
        attempts
    );

    setText(
        "dashboardAccuracy",
        `${averageAccuracy}%`
    );

    setText(
        "dashboardBestScore",
        `${bestScore}%`
    );

    setText(
        "dashboardRecentSubject",
        recentSubject
    );

    setupDailyQuote();
}


function getQuizHistory() {
    try {
        const history =
            JSON.parse(
                localStorage.getItem(
                    "quizoraQuizHistory"
                )
            );

        return Array.isArray(history)
            ? history
            : [];

    } catch (error) {
        return [];
    }
}


function saveQuizToHistory(result) {
    const history = getQuizHistory();

    history.push(result);

    localStorage.setItem(
        "quizoraQuizHistory",
        JSON.stringify(history)
    );
}


function displayRecentDashboardActivity(result) {
    const recentActivity =
        document.getElementById(
            "dashboardRecentActivity"
        );

    if (!recentActivity) {
        return;
    }

    const subject =
        result.subject || "Quiz";

    const percentage =
        Number(result.percentage) || 0;

    const correct =
        Number(result.correct) || 0;

    const total =
        Number(result.total) || 0;

    const mode =
        result.mode || "Practice Mode";

    const year =
        result.year || "";

    recentActivity.innerHTML = `
        <div class="dashboard-empty-icon">
            📊
        </div>

        <h3>
            ${subject}
        </h3>

        <p>
            Latest result:
            <strong>${percentage}%</strong>
            (${correct} / ${total} correct)
        </p>

        <p>
            ${year} • ${mode}
        </p>

        <a
            href="courses.html"
            class="dashboard-small-button"
        >
            Practice Again
        </a>
    `;
}


function setupDailyQuote() {
    const dailyQuote =
        document.getElementById("dailyQuote");

    if (!dailyQuote) {
        return;
    }

    const quotes = [
        "Success is built one question at a time.",
        "Small progress each day leads to bigger results.",
        "Practice turns difficult questions into familiar ones.",
        "Keep learning. Every question teaches you something.",
        "Consistency is one of the strongest study habits.",
        "Your next attempt is another chance to improve.",
        "Learn, practise, review and keep moving forward."
    ];

    const today = new Date();

    const dateNumber =
        today.getFullYear() * 1000 +
        Math.floor(
            (
                today -
                new Date(
                    today.getFullYear(),
                    0,
                    0
                )
            ) / 86400000
        );

    const quoteIndex =
        dateNumber % quotes.length;

    dailyQuote.textContent =
        `“${quotes[quoteIndex]}”`;
}


/* =========================================================
   COURSES
========================================================= */

function setupCourses() {
    const startQuizButton =
        document.getElementById("startQuiz");

    if (!startQuizButton) {
        return;
    }

    const savedSelection =
        getSavedSelection();

    const quizSelection = {
        learningType:
            savedSelection.learningType ||
            "BECE Preparation",

        level:
            savedSelection.level ||
            "JHS 3",

        subject:
            savedSelection.subject ||
            "Mathematics",

        source:
            savedSelection.source ||
            "Past Questions",

        year:
            savedSelection.year ||
            "2026",

        paper:
            savedSelection.paper ||
            "Objective",

        mode:
            savedSelection.mode ||
            "Practice Mode"
    };

    const yearSelect =
        document.getElementById("yearSelect");

    const paperSelect =
        document.getElementById("paperSelect");

    if (yearSelect) {
        yearSelect.value =
            quizSelection.year;
    }

    if (paperSelect) {
        paperSelect.value =
            quizSelection.paper;
    }


    function updateSelectedCards() {
        document
            .querySelectorAll(
                "[data-learning-type]"
            )
            .forEach(card => {
                const value =
                    card.getAttribute(
                        "data-learning-type"
                    );

                let selected = false;

                if (value === "bece") {
                    selected =
                        quizSelection.learningType ===
                        "BECE Preparation";
                }

                if (value === "ccp") {
                    selected =
                        quizSelection.learningType ===
                        "CCP";
                }

                card.classList.toggle(
                    "active",
                    selected
                );
            });


        document
            .querySelectorAll("[data-level]")
            .forEach(card => {
                const selected =
                    card.getAttribute(
                        "data-level"
                    ) === quizSelection.level;

                card.classList.toggle(
                    "active",
                    selected
                );
            });


        document
            .querySelectorAll("[data-subject]")
            .forEach(card => {
                const selected =
                    card.getAttribute(
                        "data-subject"
                    ) === quizSelection.subject;

                card.classList.toggle(
                    "active",
                    selected
                );
            });


        document
            .querySelectorAll("[data-source]")
            .forEach(card => {
                const value =
                    card.getAttribute(
                        "data-source"
                    );

                const selected =
                    value === "past"
                        ? quizSelection.source ===
                        "Past Questions"
                        : quizSelection.source ===
                        "CCP Questions";

                card.classList.toggle(
                    "active",
                    selected
                );
            });


        document
            .querySelectorAll("[data-mode]")
            .forEach(card => {
                const value =
                    card.getAttribute(
                        "data-mode"
                    );

                const selected =
                    value === "practice"
                        ? quizSelection.mode ===
                        "Practice Mode"
                        : quizSelection.mode ===
                        "Exam Mode";

                card.classList.toggle(
                    "active",
                    selected
                );
            });
    }


    function updateSummary() {
        setText(
            "summaryLearningType",
            quizSelection.learningType
        );

        setText(
            "summaryLevel",
            quizSelection.level
        );

        setText(
            "summarySubject",
            quizSelection.subject
        );

        setText(
            "summarySource",
            quizSelection.source
        );

        setText(
            "summaryYear",
            quizSelection.year
        );

        setText(
            "summaryPaper",
            quizSelection.paper
        );

        setText(
            "summaryMode",
            quizSelection.mode
        );
    }


    function saveSelection() {
        localStorage.setItem(
            "quizoraSelection",
            JSON.stringify(quizSelection)
        );
    }


    document
        .querySelectorAll(
            "[data-learning-type]"
        )
        .forEach(card => {
            card.addEventListener(
                "click",
                event => {
                    event.preventDefault();

                    const value =
                        card.getAttribute(
                            "data-learning-type"
                        );

                    if (value === "bece") {
                        quizSelection.learningType =
                            "BECE Preparation";
                    }

                    if (value === "ccp") {
                        quizSelection.learningType =
                            "CCP";
                    }

                    updateSelectedCards();
                    updateSummary();
                    saveSelection();
                }
            );
        });


    document
        .querySelectorAll("[data-level]")
        .forEach(card => {
            card.addEventListener(
                "click",
                event => {
                    event.preventDefault();

                    quizSelection.level =
                        card.getAttribute(
                            "data-level"
                        );

                    updateSelectedCards();
                    updateSummary();
                    saveSelection();
                }
            );
        });


    document
        .querySelectorAll("[data-subject]")
        .forEach(card => {
            card.addEventListener(
                "click",
                event => {
                    event.preventDefault();

                    quizSelection.subject =
                        card.getAttribute(
                            "data-subject"
                        );

                    updateSelectedCards();
                    updateSummary();
                    saveSelection();
                }
            );
        });


    document
        .querySelectorAll("[data-source]")
        .forEach(card => {
            card.addEventListener(
                "click",
                event => {
                    event.preventDefault();

                    const value =
                        card.getAttribute(
                            "data-source"
                        );

                    quizSelection.source =
                        value === "past"
                            ? "Past Questions"
                            : "CCP Questions";

                    updateSelectedCards();
                    updateSummary();
                    saveSelection();
                }
            );
        });


    document
        .querySelectorAll("[data-mode]")
        .forEach(card => {
            card.addEventListener(
                "click",
                event => {
                    event.preventDefault();

                    const value =
                        card.getAttribute(
                            "data-mode"
                        );

                    quizSelection.mode =
                        value === "practice"
                            ? "Practice Mode"
                            : "Exam Mode";

                    updateSelectedCards();
                    updateSummary();
                    saveSelection();
                }
            );
        });


    if (yearSelect) {
        yearSelect.addEventListener(
            "change",
            () => {
                quizSelection.year =
                    yearSelect.value;

                updateSummary();
                saveSelection();
            }
        );
    }


    if (paperSelect) {
        paperSelect.addEventListener(
            "change",
            () => {
                quizSelection.paper =
                    paperSelect.value;

                updateSummary();
                saveSelection();
            }
        );
    }


    startQuizButton.addEventListener(
        "click",
        () => {
            saveSelection();

            localStorage.removeItem(
                "quizoraLastResult"
            );

            window.location.href =
                "quiz.html";
        }
    );


    updateSelectedCards();
    updateSummary();
    saveSelection();
}


function getSavedSelection() {
    try {
        return JSON.parse(
            localStorage.getItem(
                "quizoraSelection"
            )
        ) || {};

    } catch (error) {
        return {};
    }
}


/* =========================================================
   QUESTIONS
========================================================= */

const quizQuestions = [
    {
        question:
            "What is the value of 3 × 8 + 4?",

        options: [
            "24",
            "28",
            "32",
            "36"
        ],

        answer: 1,

        explanation:
            "Multiply 3 by 8 first to get 24, then add 4 to get 28."
    },

    {
        question:
            "What is 15% of 200?",

        options: [
            "15",
            "20",
            "30",
            "40"
        ],

        answer: 2,

        explanation:
            "15% of 200 is 0.15 × 200, which equals 30."
    },

    {
        question:
            "Solve: 5x = 35. What is x?",

        options: [
            "5",
            "6",
            "7",
            "8"
        ],

        answer: 2,

        explanation:
            "Divide both sides by 5. Therefore x = 7."
    },

    {
        question:
            "A rectangle has length 8 cm and width 5 cm. What is its area?",

        options: [
            "13 cm²",
            "26 cm²",
            "40 cm²",
            "80 cm²"
        ],

        answer: 2,

        explanation:
            "Area of a rectangle is length × width. 8 × 5 = 40 cm²."
    },

    {
        question:
            "Which fraction is equivalent to 0.5?",

        options: [
            "1/4",
            "1/2",
            "2/3",
            "3/4"
        ],

        answer: 1,

        explanation:
            "0.5 is the same as one half, which is 1/2."
    },

    {
        question:
            "What is the perimeter of a square with side length 6 cm?",

        options: [
            "12 cm",
            "18 cm",
            "24 cm",
            "36 cm"
        ],

        answer: 2,

        explanation:
            "A square has four equal sides. 4 × 6 = 24 cm."
    },

    {
        question:
            "What is 144 ÷ 12?",

        options: [
            "10",
            "11",
            "12",
            "14"
        ],

        answer: 2,

        explanation:
            "144 divided by 12 equals 12."
    },

    {
        question:
            "If a book costs GH₵20 and you buy 3 books, how much do you pay?",

        options: [
            "GH₵40",
            "GH₵50",
            "GH₵60",
            "GH₵80"
        ],

        answer: 2,

        explanation:
            "3 books at GH₵20 each cost 3 × 20 = GH₵60."
    },

    {
        question:
            "Which of these numbers is a prime number?",

        options: [
            "9",
            "15",
            "17",
            "21"
        ],

        answer: 2,

        explanation:
            "17 has only two factors: 1 and 17."
    },

    {
        question:
            "What is the next number in the sequence: 2, 4, 8, 16, ...?",

        options: [
            "18",
            "24",
            "30",
            "32"
        ],

        answer: 3,

        explanation:
            "Each number is multiplied by 2. Therefore 16 × 2 = 32."
    }
];


/* =========================================================
   QUIZ
========================================================= */

function setupQuiz() {
    const questionText =
        document.getElementById("questionText");

    if (!questionText) {
        return;
    }

    const studentName =
        localStorage.getItem(
            "quizoraStudentName"
        );

    if (!studentName) {
        window.location.href = "login.html";
        return;
    }

    const selection =
        getSavedSelection();

    const quizSubject =
        document.getElementById("quizSubject");

    const quizDetails =
        document.getElementById("quizDetails");

    const quizModeBadge =
        document.getElementById("quizModeBadge");

    const timerContainer =
        document.getElementById(
            "quizTimerContainer"
        );

    const timerDisplay =
        document.getElementById("quizTimer");

    const currentQuestionNumber =
        document.getElementById(
            "currentQuestionNumber"
        );

    const totalQuestions =
        document.getElementById(
            "totalQuestions"
        );

    const questionLabelNumber =
        document.getElementById(
            "questionLabelNumber"
        );

    const progressPercentage =
        document.getElementById(
            "progressPercentage"
        );

    const progressFill =
        document.getElementById(
            "quizProgressFill"
        );

    const answerOptions =
        document.getElementById(
            "answerOptions"
        );

    const answerFeedback =
        document.getElementById(
            "answerFeedback"
        );

    const previousButton =
        document.getElementById(
            "previousQuestion"
        );

    const nextButton =
        document.getElementById(
            "nextQuestion"
        );

    const navigator =
        document.getElementById(
            "questionNavigator"
        );

    const submitButton =
        document.getElementById(
            "submitQuizButton"
        );

    const submitModal =
        document.getElementById(
            "submitModal"
        );

    const submitModalText =
        document.getElementById(
            "submitModalText"
        );

    const continueButton =
        document.getElementById(
            "continueQuizButton"
        );

    const confirmSubmitButton =
        document.getElementById(
            "confirmSubmitButton"
        );

    const subject =
        selection.subject ||
        "Mathematics";

    const learningType =
        selection.learningType ||
        "BECE Preparation";

    const level =
        selection.level ||
        "JHS 3";

    const year =
        selection.year ||
        "2026";

    const paper =
        selection.paper ||
        "Objective";

    const mode =
        selection.mode ||
        "Practice Mode";


    if (quizSubject) {
        quizSubject.textContent = subject;
    }

    if (quizDetails) {
        quizDetails.textContent =
            `${learningType} • ${level} • ${year} • ${paper}`;
    }

    if (quizModeBadge) {
        quizModeBadge.textContent = mode;
    }

    if (totalQuestions) {
        totalQuestions.textContent =
            quizQuestions.length;
    }


    let currentQuestion = 0;

    const answers =
        new Array(
            quizQuestions.length
        ).fill(null);

    let quizSubmitted = false;


    function renderQuestion() {
        const question =
            quizQuestions[currentQuestion];

        const questionNumber =
            currentQuestion + 1;

        const percentage =
            Math.round(
                (
                    questionNumber /
                    quizQuestions.length
                ) * 100
            );

        questionText.textContent =
            question.question;

        if (currentQuestionNumber) {
            currentQuestionNumber.textContent =
                questionNumber;
        }

        if (questionLabelNumber) {
            questionLabelNumber.textContent =
                questionNumber;
        }

        if (progressPercentage) {
            progressPercentage.textContent =
                `${percentage}%`;
        }

        if (progressFill) {
            progressFill.style.width =
                `${percentage}%`;
        }

        answerOptions.innerHTML = "";

        question.options.forEach(
            (option, index) => {
                const button =
                    document.createElement(
                        "button"
                    );

                button.type = "button";

                button.className =
                    "answer-option";

                if (
                    answers[currentQuestion] ===
                    index
                ) {
                    button.classList.add(
                        "selected"
                    );
                }

                const letter =
                    String.fromCharCode(
                        65 + index
                    );

                button.innerHTML = `
                    <span class="answer-letter">
                        ${letter}
                    </span>

                    <span>
                        ${option}
                    </span>
                `;

                button.addEventListener(
                    "click",
                    () => {
                        selectAnswer(index);
                    }
                );

                answerOptions.appendChild(
                    button
                );
            }
        );

        renderFeedback();
        renderNavigator();

        if (previousButton) {
            previousButton.disabled =
                currentQuestion === 0;
        }

        if (nextButton) {
            if (
                currentQuestion ===
                quizQuestions.length - 1
            ) {
                nextButton.textContent =
                    "Review →";
            } else {
                nextButton.textContent =
                    "Next →";
            }
        }
    }


    function selectAnswer(index) {
        if (quizSubmitted) {
            return;
        }

        answers[currentQuestion] =
            index;

        renderQuestion();
    }


    function renderFeedback() {
        if (!answerFeedback) {
            return;
        }

        answerFeedback.className =
            "answer-feedback";

        answerFeedback.textContent = "";

        if (mode !== "Practice Mode") {
            return;
        }

        const selectedAnswer =
            answers[currentQuestion];

        if (selectedAnswer === null) {
            return;
        }

        const question =
            quizQuestions[currentQuestion];

        const isCorrect =
            selectedAnswer ===
            question.answer;

        answerFeedback.classList.add(
            "show",
            isCorrect
                ? "correct"
                : "incorrect"
        );

        answerFeedback.textContent =
            isCorrect
                ? `Correct! ${question.explanation}`
                : `Not quite. ${question.explanation}`;

        const optionButtons =
            answerOptions.querySelectorAll(
                ".answer-option"
            );

        optionButtons.forEach(
            (button, index) => {
                if (
                    index ===
                    question.answer
                ) {
                    button.classList.add(
                        "correct"
                    );
                }

                if (
                    index === selectedAnswer &&
                    index !== question.answer
                ) {
                    button.classList.add(
                        "incorrect"
                    );
                }
            }
        );
    }


    function renderNavigator() {
        if (!navigator) {
            return;
        }

        navigator.innerHTML = "";

        quizQuestions.forEach(
            (_, index) => {
                const button =
                    document.createElement(
                        "button"
                    );

                button.type = "button";

                button.className =
                    "navigator-button";

                button.textContent =
                    index + 1;

                if (
                    index ===
                    currentQuestion
                ) {
                    button.classList.add(
                        "current"
                    );
                }

                if (
                    answers[index] !== null
                ) {
                    button.classList.add(
                        "answered"
                    );
                }

                button.addEventListener(
                    "click",
                    () => {
                        currentQuestion =
                            index;

                        renderQuestion();
                    }
                );

                navigator.appendChild(
                    button
                );
            }
        );
    }


    if (previousButton) {
        previousButton.addEventListener(
            "click",
            () => {
                if (currentQuestion > 0) {
                    currentQuestion -= 1;
                    renderQuestion();
                }
            }
        );
    }


    if (nextButton) {
        nextButton.addEventListener(
            "click",
            () => {
                if (
                    currentQuestion <
                    quizQuestions.length - 1
                ) {
                    currentQuestion += 1;
                    renderQuestion();
                } else {
                    openSubmitModal();
                }
            }
        );
    }


    function openSubmitModal() {
        if (!submitModal) {
            submitQuiz();
            return;
        }

        const unanswered =
            answers.filter(
                answer => answer === null
            ).length;

        if (submitModalText) {
            if (unanswered > 0) {
                submitModalText.textContent =
                    `You still have ${unanswered} unanswered question${unanswered === 1 ? "" : "s"}. You can continue or submit now.`;
            } else {
                submitModalText.textContent =
                    "You have answered all questions. Submit when you're ready.";
            }
        }

        submitModal.classList.add("open");
    }


    if (submitButton) {
        submitButton.addEventListener(
            "click",
            openSubmitModal
        );
    }


    if (continueButton) {
        continueButton.addEventListener(
            "click",
            () => {
                submitModal.classList.remove(
                    "open"
                );
            }
        );
    }


    if (confirmSubmitButton) {
        confirmSubmitButton.addEventListener(
            "click",
            () => {
                submitModal.classList.remove(
                    "open"
                );

                submitQuiz();
            }
        );
    }


    function submitQuiz() {
        if (quizSubmitted) {
            return;
        }

        quizSubmitted = true;

        let correct = 0;

        answers.forEach(
            (answer, index) => {
                if (
                    answer ===
                    quizQuestions[index].answer
                ) {
                    correct += 1;
                }
            }
        );

        const total =
            quizQuestions.length;

        const incorrect =
            total - correct;

        const percentage =
            Math.round(
                (correct / total) * 100
            );

        const result = {
            correct,
            incorrect,
            total,
            percentage,
            learningType,
            level,
            subject,

            source:
                selection.source ||
                "Past Questions",

            year,
            paper,
            mode,

            completedAt:
                new Date().toISOString()
        };

        localStorage.setItem(
            "quizoraLastResult",
            JSON.stringify(result)
        );

        saveQuizToHistory(result);

        window.location.href =
            "results.html";
    }


    let timerInterval = null;

    if (mode === "Exam Mode") {
        let secondsRemaining =
            20 * 60;

        if (timerContainer) {
            timerContainer.style.display =
                "block";
        }

        function updateTimer() {
            const minutes =
                Math.floor(
                    secondsRemaining / 60
                );

            const seconds =
                secondsRemaining % 60;

            if (timerDisplay) {
                timerDisplay.textContent =
                    `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
            }

            if (secondsRemaining <= 0) {
                clearInterval(
                    timerInterval
                );

                submitQuiz();
                return;
            }

            secondsRemaining -= 1;
        }

        updateTimer();

        timerInterval =
            setInterval(
                updateTimer,
                1000
            );

    } else {
        if (timerContainer) {
            timerContainer.style.display =
                "none";
        }
    }


    setupCalculator();
    renderQuestion();
}


/* =========================================================
   CALCULATOR
========================================================= */

function setupCalculator() {
    const calculatorButton =
        document.getElementById(
            "calculatorButton"
        );

    const calculatorOverlay =
        document.getElementById(
            "calculatorOverlay"
        );

    const calculatorClose =
        document.getElementById(
            "calculatorClose"
        );

    const calculatorDisplay =
        document.getElementById(
            "calculatorDisplay"
        );

    if (
        !calculatorButton ||
        !calculatorOverlay ||
        !calculatorDisplay
    ) {
        return;
    }

    let expression = "";


    calculatorButton.addEventListener(
        "click",
        () => {
            calculatorOverlay.classList.add(
                "open"
            );
        }
    );


    if (calculatorClose) {
        calculatorClose.addEventListener(
            "click",
            () => {
                calculatorOverlay.classList.remove(
                    "open"
                );
            }
        );
    }


    calculatorOverlay.addEventListener(
        "click",
        event => {
            if (
                event.target ===
                calculatorOverlay
            ) {
                calculatorOverlay.classList.remove(
                    "open"
                );
            }
        }
    );


    calculatorOverlay
        .querySelectorAll("[data-calc]")
        .forEach(button => {
            button.addEventListener(
                "click",
                () => {
                    const value =
                        button.getAttribute(
                            "data-calc"
                        );

                    handleCalculatorInput(
                        value
                    );
                }
            );
        });


    function updateDisplay(value) {
        calculatorDisplay.textContent =
            value || "0";
    }


    function handleCalculatorInput(value) {
        if (value === "clear") {
            expression = "";
            updateDisplay(expression);
            return;
        }

        if (value === "delete") {
            expression =
                expression.slice(0, -1);

            updateDisplay(expression);
            return;
        }

        if (value === "=") {
            calculateExpression();
            return;
        }

        if (value === "sqrt") {
            applyScientificFunction(
                number =>
                    Math.sqrt(number)
            );

            return;
        }

        if (value === "square") {
            applyScientificFunction(
                number =>
                    number * number
            );

            return;
        }

        if (value === "sin") {
            applyScientificFunction(
                number =>
                    Math.sin(
                        number *
                        Math.PI /
                        180
                    )
            );

            return;
        }

        if (value === "cos") {
            applyScientificFunction(
                number =>
                    Math.cos(
                        number *
                        Math.PI /
                        180
                    )
            );

            return;
        }

        expression += value;

        updateDisplay(expression);
    }


    function calculateExpression() {
        try {
            if (!expression) {
                return;
            }

            if (
                !/^[0-9+\-*/().\s]+$/.test(
                    expression
                )
            ) {
                throw new Error(
                    "Invalid expression"
                );
            }

            const result =
                Function(
                    `"use strict"; return (${expression})`
                )();

            if (
                !Number.isFinite(
                    Number(result)
                )
            ) {
                throw new Error(
                    "Invalid result"
                );
            }

            expression =
                String(
                    roundCalculatorResult(
                        Number(result)
                    )
                );

            updateDisplay(expression);

        } catch (error) {
            expression = "";
            updateDisplay("Error");
        }
    }


    function applyScientificFunction(operation) {
        try {
            if (!expression) {
                return;
            }

            if (
                !/^[0-9+\-*/().\s]+$/.test(
                    expression
                )
            ) {
                throw new Error(
                    "Invalid expression"
                );
            }

            const currentValue =
                Function(
                    `"use strict"; return (${expression})`
                )();

            const result =
                operation(
                    Number(currentValue)
                );

            if (
                !Number.isFinite(result)
            ) {
                throw new Error(
                    "Invalid result"
                );
            }

            expression =
                String(
                    roundCalculatorResult(
                        result
                    )
                );

            updateDisplay(expression);

        } catch (error) {
            expression = "";
            updateDisplay("Error");
        }
    }
}


function roundCalculatorResult(number) {
    return Math.round(
        (
            number +
            Number.EPSILON
        ) * 100000000
    ) / 100000000;
}


/* =========================================================
   RESULTS
========================================================= */

function setupResults() {
    const resultPercentage =
        document.getElementById(
            "resultPercentage"
        );

    if (!resultPercentage) {
        return;
    }

    const studentName =
        localStorage.getItem(
            "quizoraStudentName"
        );

    if (!studentName) {
        window.location.href =
            "login.html";

        return;
    }

    let result = {};

    try {
        result =
            JSON.parse(
                localStorage.getItem(
                    "quizoraLastResult"
                )
            ) || {};

    } catch (error) {
        result = {};
    }

    const total =
        Number(result.total) ||
        quizQuestions.length;

    const correct =
        Number(result.correct) || 0;

    const incorrect =
        Number.isFinite(
            Number(result.incorrect)
        )
            ? Number(result.incorrect)
            : total - correct;

    const percentage =
        Number(result.percentage) || 0;


    setText(
        "resultPercentage",
        `${percentage}%`
    );

    setText(
        "correctAnswers",
        correct
    );

    setText(
        "incorrectAnswers",
        incorrect
    );

    setText(
        "resultTotalQuestions",
        total
    );

    setText(
        "resultAccuracy",
        `${percentage}%`
    );

    setText(
        "resultLearningType",
        result.learningType ||
        "BECE Preparation"
    );

    setText(
        "resultLevel",
        result.level ||
        "JHS 3"
    );

    setText(
        "resultSubject",
        result.subject ||
        "Mathematics"
    );

    setText(
        "resultSource",
        result.source ||
        "Past Questions"
    );

    setText(
        "resultYear",
        result.year ||
        "2026"
    );

    setText(
        "resultPaper",
        result.paper ||
        "Objective"
    );

    setText(
        "resultMode",
        result.mode ||
        "Practice Mode"
    );

    setText(
        "resultScoreText",
        `${correct} / ${total}`
    );


    const message =
        getResultMessage(
            percentage
        );

    setText(
        "resultMessage",
        message.title
    );

    setText(
        "resultMessageText",
        message.text
    );


    const retryButton =
        document.getElementById(
            "retryQuizButton"
        );

    if (retryButton) {
        retryButton.addEventListener(
            "click",
            () => {
                localStorage.removeItem(
                    "quizoraLastResult"
                );

                window.location.href =
                    "quiz.html";
            }
        );
    }
}


function getResultMessage(percentage) {
    if (percentage >= 80) {
        return {
            title:
                "Excellent performance!",

            text:
                "You showed a strong understanding of this practice session. Keep building on it."
        };
    }

    if (percentage >= 60) {
        return {
            title:
                "Good progress!",

            text:
                "You're making good progress. Review the areas you missed and try another session."
        };
    }

    if (percentage >= 40) {
        return {
            title:
                "Keep practicing!",

            text:
                "You're building your understanding. Review the difficult areas and practice again."
        };
    }

    return {
        title:
            "Every session helps.",

        text:
            "Review the topics you found difficult and keep practicing to strengthen your understanding."
    };
}


/* =========================================================
   GENERAL HELPER
========================================================= */

function setText(id, value) {
    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}
/* =========================================================
   INSTITUTIONAL ADMIN DASHBOARD
========================================================= */

function setupAdminDashboard() {
    const studentTableBody =
        document.getElementById("adminStudentTableBody");

    if (!studentTableBody) {
        return;
    }

    /*
        FRONTEND DEMO DATA

        Later, the backend developer will replace this
        sample data with students belonging to the
        logged-in institution.
    */
    const institutionData = {
        name: "Quizora Institution",
        code: "QUIZORA-2026"
    };

    const students = [
        {
            name: "Ama Mensah",
            email: "ama@example.com",
            attempts: 8,
            average: 82,
            bestScore: 95,
            recentSubject: "Mathematics",
            status: "Active"
        },
        {
            name: "Kwame Asante",
            email: "kwame@example.com",
            attempts: 6,
            average: 74,
            bestScore: 88,
            recentSubject: "Science",
            status: "Active"
        },
        {
            name: "Akosua Owusu",
            email: "akosua@example.com",
            attempts: 10,
            average: 91,
            bestScore: 100,
            recentSubject: "English Language",
            status: "Active"
        },
        {
            name: "Kojo Addo",
            email: "kojo@example.com",
            attempts: 4,
            average: 68,
            bestScore: 80,
            recentSubject: "Social Studies",
            status: "Active"
        },
        {
            name: "Abena Boateng",
            email: "abena@example.com",
            attempts: 7,
            average: 79,
            bestScore: 90,
            recentSubject: "Computing",
            status: "Active"
        }
    ];


    /* =====================================================
       INSTITUTION INFORMATION
    ===================================================== */

    const institutionName =
        localStorage.getItem("quizoraInstitutionName") ||
        institutionData.name;

    const institutionCode =
        localStorage.getItem("quizoraInstitutionCode") ||
        institutionData.code;

    setText(
        "adminInstitutionName",
        institutionName
    );

    setText(
        "adminInstitutionCode",
        institutionCode
    );


    /* =====================================================
       STATISTICS
    ===================================================== */

    function updateAdminStatistics() {
        const totalStudents =
            students.length;

        const totalAttempts =
            students.reduce(
                (total, student) =>
                    total + student.attempts,
                0
            );

        const averageScore =
            totalStudents > 0
                ? Math.round(
                    students.reduce(
                        (total, student) =>
                            total + student.average,
                        0
                    ) / totalStudents
                )
                : 0;

        const bestScore =
            totalStudents > 0
                ? Math.max(
                    ...students.map(
                        student =>
                            student.bestScore
                    )
                )
                : 0;

        setText(
            "adminTotalStudents",
            totalStudents
        );

        setText(
            "adminQuizAttempts",
            totalAttempts
        );

        setText(
            "adminAverageScore",
            `${averageScore}%`
        );

        setText(
            "adminBestScore",
            `${bestScore}%`
        );
    }


    /* =====================================================
       STUDENT TABLE
    ===================================================== */

    function renderStudents(studentList) {
        studentTableBody.innerHTML = "";

        if (studentList.length === 0) {
            studentTableBody.innerHTML = `
                <tr class="admin-empty-row">
                    <td colspan="7">
                        No students found.
                    </td>
                </tr>
            `;

            return;
        }

        studentList.forEach(student => {
            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${student.name}</td>

                <td>
                    ${student.email}
                </td>

                <td>
                    ${student.attempts}
                </td>

                <td>
                    ${student.average}%
                </td>

                <td>
                    ${student.bestScore}%
                </td>

                <td>
                    ${student.recentSubject}
                </td>

                <td>
                    <span class="admin-status-badge">
                        ${student.status}
                    </span>
                </td>
            `;

            studentTableBody.appendChild(row);
        });
    }


    /* =====================================================
       SEARCH STUDENTS
    ===================================================== */

    const searchInput =
        document.getElementById(
            "adminStudentSearch"
        );

    if (searchInput) {
        searchInput.addEventListener(
            "input",
            () => {
                const searchTerm =
                    searchInput.value
                        .trim()
                        .toLowerCase();

                const filteredStudents =
                    students.filter(student => {
                        return (
                            student.name
                                .toLowerCase()
                                .includes(searchTerm) ||

                            student.email
                                .toLowerCase()
                                .includes(searchTerm) ||

                            student.recentSubject
                                .toLowerCase()
                                .includes(searchTerm)
                        );
                    });

                renderStudents(
                    filteredStudents
                );
            }
        );
    }


    /* =====================================================
       GENERATE REPORT
    ===================================================== */

    const generateReportButton =
        document.getElementById(
            "generateAdminReport"
        );

    const reportPreview =
        document.getElementById(
            "adminReportPreview"
        );

    if (
        generateReportButton &&
        reportPreview
    ) {
        generateReportButton.addEventListener(
            "click",
            () => {
                const totalStudents =
                    students.length;

                const totalAttempts =
                    students.reduce(
                        (total, student) =>
                            total +
                            student.attempts,
                        0
                    );

                const averageScore =
                    totalStudents > 0
                        ? Math.round(
                            students.reduce(
                                (
                                    total,
                                    student
                                ) =>
                                    total +
                                    student.average,
                                0
                            ) /
                            totalStudents
                        )
                        : 0;

                const bestScore =
                    totalStudents > 0
                        ? Math.max(
                            ...students.map(
                                student =>
                                    student.bestScore
                            )
                        )
                        : 0;

                reportPreview.innerHTML = `
                    <div class="admin-report-preview-icon">
                        📊
                    </div>

                    <h3>
                        ${institutionName}
                        Performance Report
                    </h3>

                    <p>
                        Institution Code:
                        <strong>
                            ${institutionCode}
                        </strong>
                    </p>

                    <p>
                        Enrolled Students:
                        <strong>
                            ${totalStudents}
                        </strong>
                        &nbsp; • &nbsp;

                        Quiz Attempts:
                        <strong>
                            ${totalAttempts}
                        </strong>
                        &nbsp; • &nbsp;

                        Average Score:
                        <strong>
                            ${averageScore}%
                        </strong>
                        &nbsp; • &nbsp;

                        Best Score:
                        <strong>
                            ${bestScore}%
                        </strong>
                    </p>
                `;
            }
        );
    }


    /* =====================================================
       CSV EXPORT
    ===================================================== */

    const csvButton =
        document.getElementById(
            "exportAdminCSV"
        );

    if (csvButton) {
        csvButton.addEventListener(
            "click",
            () => {
                const headings = [
                    "Student",
                    "Email",
                    "Quiz Attempts",
                    "Average Score",
                    "Best Score",
                    "Recent Subject",
                    "Status"
                ];

                const rows =
                    students.map(student => [
                        student.name,
                        student.email,
                        student.attempts,
                        `${student.average}%`,
                        `${student.bestScore}%`,
                        student.recentSubject,
                        student.status
                    ]);

                const csvRows = [
                    headings,
                    ...rows
                ];

                const csvContent =
                    csvRows
                        .map(row =>
                            row
                                .map(value =>
                                    `"${String(value)
                                        .replace(
                                            /"/g,
                                            '""'
                                        )}"`
                                )
                                .join(",")
                        )
                        .join("\n");

                const blob =
                    new Blob(
                        [csvContent],
                        {
                            type:
                                "text/csv;charset=utf-8;"
                        }
                    );

                const url =
                    URL.createObjectURL(blob);

                const downloadLink =
                    document.createElement("a");

                downloadLink.href = url;

                downloadLink.download =
                    "quizora-institution-report.csv";

                document.body.appendChild(
                    downloadLink
                );

                downloadLink.click();

                downloadLink.remove();

                URL.revokeObjectURL(url);
            }
        );
    }


    /* =====================================================
       PDF / PRINT REPORT
    ===================================================== */

    const pdfButton =
        document.getElementById(
            "exportAdminPDF"
        );

    if (pdfButton) {
        pdfButton.addEventListener(
            "click",
            () => {
                const reportWindow =
                    window.open(
                        "",
                        "_blank"
                    );

                if (!reportWindow) {
                    alert(
                        "Please allow pop-ups to generate the report."
                    );

                    return;
                }

                const totalAttempts =
                    students.reduce(
                        (total, student) =>
                            total +
                            student.attempts,
                        0
                    );

                const averageScore =
                    students.length > 0
                        ? Math.round(
                            students.reduce(
                                (
                                    total,
                                    student
                                ) =>
                                    total +
                                    student.average,
                                0
                            ) /
                            students.length
                        )
                        : 0;

                const studentRows =
                    students
                        .map(student => `
                            <tr>
                                <td>
                                    ${student.name}
                                </td>

                                <td>
                                    ${student.email}
                                </td>

                                <td>
                                    ${student.attempts}
                                </td>

                                <td>
                                    ${student.average}%
                                </td>

                                <td>
                                    ${student.bestScore}%
                                </td>

                                <td>
                                    ${student.recentSubject}
                                </td>
                            </tr>
                        `)
                        .join("");

                reportWindow.document.write(`
                    <!DOCTYPE html>

                    <html lang="en">

                    <head>
                        <meta charset="UTF-8">

                        <title>
                            Quizora Institution Report
                        </title>

                        <style>
                            body {
                                font-family:
                                    Arial,
                                    sans-serif;

                                padding: 40px;
                                color: #172033;
                            }

                            h1 {
                                color: #1d4ed8;
                                margin-bottom: 5px;
                            }

                            .report-subtitle {
                                color: #64748b;
                                margin-bottom: 30px;
                            }

                            .summary {
                                display: flex;
                                gap: 30px;
                                margin: 25px 0;
                            }

                            .summary div {
                                padding: 15px;
                                border:
                                    1px solid
                                    #e2e8f0;
                            }

                            table {
                                width: 100%;
                                border-collapse:
                                    collapse;
                                margin-top: 25px;
                            }

                            th,
                            td {
                                border:
                                    1px solid
                                    #dbe3ed;

                                padding: 10px;
                                text-align: left;
                                font-size: 13px;
                            }

                            th {
                                background:
                                    #eff6ff;
                            }

                            .footer {
                                margin-top: 30px;
                                color: #64748b;
                                font-size: 12px;
                            }

                            @media print {
                                button {
                                    display: none;
                                }
                            }
                        </style>
                    </head>

                    <body>

                        <h1>
                            ${institutionName}
                        </h1>

                        <p class="report-subtitle">
                            Quizora Institutional
                            Performance Report
                        </p>

                        <p>
                            <strong>
                                Institution Code:
                            </strong>

                            ${institutionCode}
                        </p>

                        <div class="summary">

                            <div>
                                <strong>
                                    Students
                                </strong>

                                <br>

                                ${students.length}
                            </div>

                            <div>
                                <strong>
                                    Quiz Attempts
                                </strong>

                                <br>

                                ${totalAttempts}
                            </div>

                            <div>
                                <strong>
                                    Average Score
                                </strong>

                                <br>

                                ${averageScore}%
                            </div>

                        </div>

                        <table>

                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Email</th>
                                    <th>Attempts</th>
                                    <th>Average</th>
                                    <th>Best</th>
                                    <th>Subject</th>
                                </tr>
                            </thead>

                            <tbody>
                                ${studentRows}
                            </tbody>

                        </table>

                        <p class="footer">
                            Generated by Quizora
                            Institutional Portal
                        </p>

                        <button
                            onclick="window.print()"
                        >
                            Print / Save as PDF
                        </button>

                    </body>

                    </html>
                `);

                reportWindow.document.close();
            }
        );
    }


    /* =====================================================
       ADMIN LOGOUT
    ===================================================== */

    const adminLogoutButton =
        document.getElementById(
            "adminLogoutButton"
        );

    if (adminLogoutButton) {
        adminLogoutButton.addEventListener(
            "click",
            () => {
                /*
                    Later the backend will destroy
                    the institution admin session.
                */

                window.location.href =
                    "login.html";
            }
        );
    }


    /* =====================================================
       INITIAL LOAD
    ===================================================== */

    updateAdminStatistics();
    renderStudents(students);
}