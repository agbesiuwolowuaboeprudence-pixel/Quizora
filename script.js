document.addEventListener("DOMContentLoaded", () => {
    const studentName =
        localStorage.getItem("quizoraStudentName") || "Student";

    const studentEmail =
        localStorage.getItem("quizoraStudentEmail") || "";

    const isLoggedIn =
        Boolean(localStorage.getItem("quizoraStudentName"));

    document.querySelectorAll("[data-student-name]").forEach(element => {
        element.textContent = studentName;
    });

    setupMobileMenu();
    setupHomePage(isLoggedIn);
    setupLogout();
    setupLogin();
    setupCourses();
    setupQuiz();
    setupResults();
});


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
            heroStartButton.href = "courses.html";
            heroStartButton.textContent = "Continue Learning →";
        }

        if (ctaStartButton) {
            ctaStartButton.href = "courses.html";
            ctaStartButton.textContent = "Continue Learning →";
        }
    }
}


function setupLogout() {
    document.querySelectorAll("[data-logout]").forEach(button => {
        button.addEventListener("click", () => {
            localStorage.removeItem("quizoraStudentName");
            localStorage.removeItem("quizoraStudentEmail");
            localStorage.removeItem("quizoraSelection");
            localStorage.removeItem("quizoraLastResult");

            window.location.href = "login.html";
        });
    });
}


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
        yearSelect.value = quizSelection.year;
    }

    if (paperSelect) {
        paperSelect.value = quizSelection.paper;
    }

    function updateSelectedCards() {
        document
            .querySelectorAll("[data-learning-type]")
            .forEach(card => {
                const value =
                    card.getAttribute("data-learning-type");

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
                    card.getAttribute("data-level") ===
                    quizSelection.level;

                card.classList.toggle(
                    "active",
                    selected
                );
            });

        document
            .querySelectorAll("[data-subject]")
            .forEach(card => {
                const selected =
                    card.getAttribute("data-subject") ===
                    quizSelection.subject;

                card.classList.toggle(
                    "active",
                    selected
                );
            });

        document
            .querySelectorAll("[data-source]")
            .forEach(card => {
                const value =
                    card.getAttribute("data-source");

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
                    card.getAttribute("data-mode");

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
        .querySelectorAll("[data-learning-type]")
        .forEach(card => {
            card.addEventListener("click", event => {
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
            });
        });

    document
        .querySelectorAll("[data-level]")
        .forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();

                quizSelection.level =
                    card.getAttribute("data-level");

                updateSelectedCards();
                updateSummary();
                saveSelection();
            });
        });

    document
        .querySelectorAll("[data-subject]")
        .forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();

                quizSelection.subject =
                    card.getAttribute("data-subject");

                updateSelectedCards();
                updateSummary();
                saveSelection();
            });
        });

    document
        .querySelectorAll("[data-source]")
        .forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();

                const value =
                    card.getAttribute("data-source");

                quizSelection.source =
                    value === "past"
                        ? "Past Questions"
                        : "CCP Questions";

                updateSelectedCards();
                updateSummary();
                saveSelection();
            });
        });

    document
        .querySelectorAll("[data-mode]")
        .forEach(card => {
            card.addEventListener("click", event => {
                event.preventDefault();

                const value =
                    card.getAttribute("data-mode");

                quizSelection.mode =
                    value === "practice"
                        ? "Practice Mode"
                        : "Exam Mode";

                updateSelectedCards();
                updateSummary();
                saveSelection();
            });
        });

    if (yearSelect) {
        yearSelect.addEventListener("change", () => {
            quizSelection.year =
                yearSelect.value;

            updateSummary();
            saveSelection();
        });
    }

    if (paperSelect) {
        paperSelect.addEventListener("change", () => {
            quizSelection.paper =
                paperSelect.value;

            updateSummary();
            saveSelection();
        });
    }

    startQuizButton.addEventListener("click", () => {
        saveSelection();

        localStorage.removeItem(
            "quizoraLastResult"
        );

        window.location.href = "quiz.html";
    });

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

        previousButton.disabled =
            currentQuestion === 0;

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

    previousButton.addEventListener(
        "click",
        () => {
            if (currentQuestion > 0) {
                currentQuestion -= 1;
                renderQuestion();
            }
        }
    );

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
            mode
        };

        localStorage.setItem(
            "quizoraLastResult",
            JSON.stringify(result)
        );

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

    function applyScientificFunction(
        operation
    ) {
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


function setupResults() {
    const resultPercentage =
        document.getElementById(
            "resultPercentage"
        );

    if (!resultPercentage) {
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


function setText(id, value) {
    const element =
        document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}