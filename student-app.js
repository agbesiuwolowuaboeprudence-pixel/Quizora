(function () {
    const API = window.QuizoraApi;
    const ATTEMPT_KEY = "quizoraAttemptId";
    const QUESTION_INDEX_KEY = "quizoraQuestionIndex";
    const SELECTION_KEY = "quizoraSelection";

    function setText(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = value;
        }
    }

    function showMessage(element, message, type) {
        if (!element) {
            return;
        }

        element.textContent = message;
        element.className = `api-message ${type || "error"}`;
        element.hidden = false;
    }

    function getErrorMessage(error) {
        if (error instanceof API.ApiError) {
            return error.message;
        }

        if (error instanceof TypeError) {
            return "Could not connect to the API. Check the API URL and your connection, then try again.";
        }

        return error.message || "The request could not be completed.";
    }

    function requireAuthentication() {
        if (API.isAuthenticated()) {
            return true;
        }

        window.location.assign("login.html");
        return false;
    }

    function saveSelection(selection) {
        localStorage.setItem(
            SELECTION_KEY,
            JSON.stringify(selection)
        );
    }

    function readSelection() {
        const value = localStorage.getItem(SELECTION_KEY);
        return value ? JSON.parse(value) : {};
    }

    function redirectToDashboard() {
        window.location.assign("Dashboard.html");
    }

    function setupLogin() {
        const form = document.getElementById("loginForm");

        if (!form) {
            return;
        }

        const emailInput = document.getElementById("studentEmail");
        const passwordInput = document.getElementById("studentPassword");
        const passwordToggle = document.getElementById("passwordToggle");
        const rememberMe = document.getElementById("rememberMe");
        const message = document.getElementById("loginMessage");

        if (emailInput) {
            emailInput.value = API.getAuthEmail() || "";
        }

        if (passwordToggle && passwordInput) {
            passwordToggle.addEventListener("click", () => {
                const visible = passwordInput.type === "text";
                passwordInput.type = visible ? "password" : "text";
                passwordToggle.textContent = visible ? "Show" : "Hide";
                passwordToggle.setAttribute(
                    "aria-label",
                    visible ? "Show password" : "Hide password"
                );
            });
        }

        form.addEventListener("submit", async event => {
            event.preventDefault();
            message.textContent = "";
            message.className = "form-message";

            const email = emailInput.value.trim();
            const password = passwordInput.value;
            const submitButton = form.querySelector('[type="submit"]');

            if (!email || !password) {
                showMessage(message, "Enter your email and password.");
                return;
            }

            submitButton.disabled = true;

            try {
                const response = await API.login({ email, password });

                if (
                    response.user.role === "INSTITUTION_ADMIN" &&
                    Number.isSafeInteger(response.user.institutionId)
                ) {
                    API.setAuthSession(response, Boolean(rememberMe.checked));
                    window.location.assign("admin-dashboard.html");
                    return;
                }

                if (response.user.role !== "STUDENT") {
                    throw new Error(
                        "Use the institution administrator login for this account."
                    );
                }

                API.setAuthSession(response, Boolean(rememberMe.checked));
                redirectToDashboard();
            } catch (error) {
                showMessage(message, getErrorMessage(error));
            } finally {
                submitButton.disabled = false;
            }
        });
    }

    function setupRegistration() {
        const stepOne = document.getElementById("registerStepOne");
        const stepTwo = document.getElementById("registerStepTwo");
        const stepThree = document.getElementById("registerStepThree");

        if (!stepOne || !stepTwo || !stepThree) {
            return;
        }

        const progressItems = document.querySelectorAll(
            ".register-progress-item"
        );
        const accountTypeCards = document.querySelectorAll(
            "[data-account-type]"
        );
        const individualDetailsSection = document.getElementById(
            "individualDetailsSection"
        );
        const institutionCodeSection = document.getElementById(
            "institutionCodeSection"
        );
        const individualAccess = document.getElementById("individualAccess");
        const institutionalAccess = document.getElementById(
            "institutionalAccess"
        );
        const detailsForm = document.getElementById(
            "registrationDetailsForm"
        );
        const institutionForm = document.getElementById(
            "institutionStudentForm"
        );
        const registerMessage = document.getElementById("registerMessage");
        const institutionMessage = document.getElementById(
            "institutionMessage"
        );
        const institutionStudentMessage = document.getElementById(
            "institutionStudentMessage"
        );
        const institutionCodeInput = document.getElementById(
            "institutionCode"
        );
        const nameInput = document.getElementById("registerName");
        const emailInput = document.getElementById("registerEmail");
        const passwordInput = document.getElementById("registerPassword");
        const confirmPasswordInput = document.getElementById(
            "registerConfirmPassword"
        );
        const completeIndividualButton = document.getElementById(
            "completeIndividualRegistration"
        );
        const accountTypeContinue = document.getElementById(
            "accountTypeContinue"
        );
        const registrationBackButton = document.getElementById(
            "registerBackOne"
        );
        const individualBackButton = document.getElementById(
            "individualBackButton"
        );
        const institutionBackButton = document.getElementById(
            "institutionBackButton"
        );
        const institutionDetailsBackButton = document.getElementById(
            "institutionDetailsBackButton"
        );
        const verifyInstitutionButton = document.getElementById(
            "verifyInstitutionButton"
        );
        const registerPasswordToggle = document.getElementById(
            "registerPasswordToggle"
        );
        const institutionPasswordToggle = document.getElementById(
            "institutionStudentPasswordToggle"
        );
        const institutionStudentPassword = document.getElementById(
            "institutionStudentPassword"
        );

        let accountType = "individual";
        let institutionCode = "";
        let individualDetails = null;

        function showStep(step) {
            [stepOne, stepTwo, stepThree].forEach((element, index) => {
                element.classList.toggle("active", index + 1 === step);
            });

            progressItems.forEach((item, index) => {
                item.classList.toggle("active", index < step);
            });

            window.scrollTo({ top: 0, behavior: "smooth" });
        }

        function showRegistrationError(element, error) {
            showMessage(element, getErrorMessage(error));
        }

        function setAccountType(value) {
            accountType = value;
            accountTypeCards.forEach(card => {
                card.classList.toggle(
                    "active",
                    card.getAttribute("data-account-type") === value
                );
            });
        }

        function prepareStepTwo() {
            const isIndividual = accountType === "individual";

            if (individualDetailsSection) {
                individualDetailsSection.style.display = isIndividual
                    ? "block"
                    : "none";
            }

            if (institutionCodeSection) {
                institutionCodeSection.style.display = isIndividual
                    ? "none"
                    : "block";
            }

            showStep(2);
        }

        function prepareStepThree() {
            const isIndividual = accountType === "individual";

            if (individualAccess) {
                individualAccess.style.display = isIndividual
                    ? "block"
                    : "none";
            }

            if (institutionalAccess) {
                institutionalAccess.style.display = isIndividual
                    ? "none"
                    : "block";
            }

            showStep(3);
        }

        function validatePassword(password, confirmation, messageElement) {
            if (password.length < 6) {
                showMessage(
                    messageElement,
                    "Password must contain at least 6 characters."
                );
                return false;
            }

            if (password !== confirmation) {
                showMessage(messageElement, "The passwords do not match.");
                return false;
            }

            return true;
        }

        async function finishRegistration(details, institutional) {
            const button = institutional
                ? institutionForm.querySelector('[type="submit"]')
                : completeIndividualButton;

            button.disabled = true;

            try {
                const response = institutional
                    ? await API.registerInstitutional(details)
                    : await API.register(details);

                API.setAuthSession(response, true);
                redirectToDashboard();
            } catch (error) {
                showRegistrationError(
                    institutional
                        ? institutionStudentMessage
                        : registerMessage,
                    error
                );
            } finally {
                button.disabled = false;
            }
        }

        accountTypeCards.forEach(card => {
            card.addEventListener("click", () => {
                setAccountType(
                    card.getAttribute("data-account-type") || "individual"
                );
            });
        });

        accountTypeContinue.addEventListener("click", prepareStepTwo);
        registrationBackButton.addEventListener("click", () => showStep(1));

        detailsForm.addEventListener("submit", event => {
            event.preventDefault();
            registerMessage.textContent = "";

            const details = {
                fullName: nameInput.value.trim(),
                email: emailInput.value.trim(),
                password: passwordInput.value
            };

            if (!details.fullName || !details.email || !details.password) {
                showMessage(registerMessage, "Complete all required fields.");
                return;
            }

            if (!validatePassword(
                details.password,
                confirmPasswordInput.value,
                registerMessage
            )) {
                return;
            }

            individualDetails = details;
            prepareStepThree();
        });

        verifyInstitutionButton.addEventListener("click", () => {
            institutionMessage.textContent = "";
            institutionCode = institutionCodeInput.value.trim();

            if (!institutionCode) {
                showMessage(
                    institutionMessage,
                    "Enter the institutional access code."
                );
                return;
            }

            prepareStepThree();
        });

        completeIndividualButton.addEventListener("click", () => {
            if (!individualDetails) {
                prepareStepTwo();
                return;
            }

            finishRegistration(individualDetails, false);
        });

        institutionForm.addEventListener("submit", event => {
            event.preventDefault();
            institutionStudentMessage.textContent = "";

            const details = {
                code: institutionCode,
                fullName: document.getElementById(
                    "institutionStudentName"
                ).value.trim(),
                email: document.getElementById(
                    "institutionStudentEmail"
                ).value.trim(),
                password: institutionStudentPassword.value
            };
            const confirmation = document.getElementById(
                "institutionStudentConfirmPassword"
            ).value;

            if (!details.code || !details.fullName || !details.email) {
                showMessage(
                    institutionStudentMessage,
                    "Complete all required fields and verify your access code."
                );
                return;
            }

            if (!validatePassword(
                details.password,
                confirmation,
                institutionStudentMessage
            )) {
                return;
            }

            finishRegistration(details, true);
        });

        [individualBackButton, institutionBackButton,
            institutionDetailsBackButton].forEach(button => {
            if (button) {
                button.addEventListener("click", prepareStepTwo);
            }
        });

        [
            [registerPasswordToggle, passwordInput],
            [institutionPasswordToggle, institutionStudentPassword]
        ].forEach(([toggle, input]) => {
            if (!toggle || !input) {
                return;
            }

            toggle.addEventListener("click", () => {
                const visible = input.type === "text";
                input.type = visible ? "password" : "text";
                toggle.textContent = visible ? "Show" : "Hide";
            });
        });

        showStep(1);
    }

    function setupAdminLogin() {
        const form = document.getElementById("adminLoginForm");

        if (!form) {
            return;
        }

        const emailInput = document.getElementById("adminEmail");
        const passwordInput = document.getElementById("adminPassword");
        const passwordToggle = document.getElementById(
            "adminPasswordToggle"
        );
        const message = document.getElementById("adminLoginMessage");

        if (passwordToggle) {
            passwordToggle.addEventListener("click", () => {
                const visible = passwordInput.type === "text";
                passwordInput.type = visible ? "password" : "text";
                passwordToggle.textContent = visible ? "Show" : "Hide";
            });
        }

        form.addEventListener("submit", async event => {
            event.preventDefault();
            message.textContent = "";

            try {
                const response = await API.login({
                    email: emailInput.value.trim(),
                    password: passwordInput.value
                });

                if (
                    response.user.role !== "INSTITUTION_ADMIN" ||
                    !Number.isSafeInteger(response.user.institutionId)
                ) {
                    throw new Error(
                        "This account is not linked to an institution administrator."
                    );
                }

                API.setAuthSession(response, true);
                window.location.assign("admin-dashboard.html");
            } catch (error) {
                message.textContent = getErrorMessage(error);
                message.className = "admin-login-message error";
            }
        });
    }

    async function setupDashboard() {
        const attemptsElement = document.getElementById("dashboardAttempts");

        if (!attemptsElement) {
            return;
        }

        if (!requireAuthentication()) {
            return;
        }

        try {
            const [dashboard, progress] = await Promise.all([
                API.getDashboard(),
                API.getProgress()
            ]);

            if (!dashboard || !dashboard.analytics || !progress.totals) {
                throw new Error(
                    "The dashboard response did not match the documented API contract."
                );
            }

            const name = dashboard.fullName;
            if (typeof name === "string") {
                document.querySelectorAll("[data-student-name]").forEach(
                    element => {
                        element.textContent = name;
                    }
                );
            }

            setText(
                "dashboardAttempts",
                progress.totals.attemptsCompleted
            );
            setText(
                "dashboardAccuracy",
                `${Math.round(progress.totals.accuracyPercent)}%`
            );
            setText(
                "dashboardBestScore",
                `${Math.round(dashboard.analytics.averageScorePercent)}%`
            );
            setText(
                "dashboardRecentSubject",
                progress.recentAttempts.length
                    ? progress.recentAttempts[0].courseName
                    : "—"
            );

            const quote = document.getElementById("dailyQuote");
            if (quote && dashboard.quote) {
                quote.textContent = `“${dashboard.quote.text}”`;
            }

            renderRecentActivity(progress.recentAttempts);
        } catch (error) {
            const activity = document.getElementById(
                "dashboardRecentActivity"
            );
            showMessage(activity, getErrorMessage(error));
        }
    }

    async function setupAdminDashboard() {
        const tableBody = document.getElementById("adminStudentTableBody");

        if (!tableBody) {
            return;
        }

        const institutionId = API.getInstitutionId();
        if (
            !API.isAuthenticated() ||
            API.getAuthRole() !== "INSTITUTION_ADMIN" ||
            !Number.isSafeInteger(institutionId) ||
            institutionId <= 0
        ) {
            window.location.assign("admin-login.html");
            return;
        }

        const preview = document.getElementById("adminReportPreview");
        const status = document.createElement("p");
        status.className = "api-message";
        status.setAttribute("role", "status");
        status.setAttribute("aria-live", "polite");
        preview.appendChild(status);

        try {
            const [institution, report] = await Promise.all([
                API.getInstitution(institutionId),
                API.getInstitutionSummary(institutionId)
            ]);

            if (
                !institution ||
                !Array.isArray(institution.licenses) ||
                !report ||
                !report.totals ||
                !Array.isArray(report.students)
            ) {
                throw new Error(
                    "The institution response did not match the documented API contract."
                );
            }

            setText("adminInstitutionName", institution.name);
            setText(
                "adminInstitutionCode",
                institution.licenses.length
                    ? institution.licenses.map(license => license.code).join(", ")
                    : "No licence codes issued"
            );
            setText("adminTotalStudents", report.totals.students);
            setText("adminQuizAttempts", report.totals.attempts);
            setText(
                "adminAverageScore",
                `${Math.round(report.totals.accuracyPercent)}%`
            );
            setText(
                "adminBestScore",
                `${report.totals.licenseSeatsUsed} / ${report.totals.licenseSeatsTotal}`
            );

            const search = document.getElementById("adminStudentSearch");
            function renderStudents(query) {
                tableBody.replaceChildren();
                const normalizedQuery = query.toLocaleLowerCase();
                const filtered = report.students.filter(student =>
                    `${student.fullName} ${student.email}`
                        .toLocaleLowerCase()
                        .includes(normalizedQuery)
                );

                if (!filtered.length) {
                    const row = document.createElement("tr");
                    row.className = "admin-empty-row";
                    const cell = document.createElement("td");
                    cell.colSpan = 7;
                    cell.textContent = "No students found.";
                    row.appendChild(cell);
                    tableBody.appendChild(row);
                    return;
                }

                filtered.forEach(student => {
                    const row = document.createElement("tr");
                    const values = [
                        student.fullName,
                        student.email,
                        student.attempts,
                        `${Math.round(student.averageScorePercent)}%`,
                        student.questionsAttempted,
                        student.lastActiveAt
                            ? new Date(student.lastActiveAt).toLocaleString()
                            : "—",
                        student.subscriptionStatus || "—"
                    ];

                    values.forEach(value => {
                        const cell = document.createElement("td");
                        cell.textContent = String(value);
                        row.appendChild(cell);
                    });

                    tableBody.appendChild(row);
                });
            }

            renderStudents("");
            search.addEventListener("input", () => {
                renderStudents(search.value);
            });

            preview.replaceChildren();
            const heading = document.createElement("h3");
            heading.textContent = "Institution report summary";
            const details = document.createElement("p");
            details.textContent =
                `${report.totals.students} students • ${report.totals.attempts} attempts • ${report.totals.licenseSeatsUsed} of ${report.totals.licenseSeatsTotal} licence seats used`;
            preview.append(heading, details, status);

            [
                ["exportAdminCSV", "csv"],
                ["exportAdminPDF", "pdf"]
            ].forEach(([buttonId, format]) => {
                const button = document.getElementById(buttonId);
                button.addEventListener("click", async () => {
                    button.disabled = true;
                    status.textContent = "Preparing report…";

                    try {
                        await API.downloadInstitutionReport(
                            institutionId,
                            format
                        );
                        status.textContent =
                            `${format.toUpperCase()} report downloaded.`;
                        status.className = "api-message success";
                    } catch (error) {
                        status.textContent = getErrorMessage(error);
                        status.className = "api-message";
                    } finally {
                        button.disabled = false;
                    }
                });
            });
        } catch (error) {
            status.textContent = getErrorMessage(error);
        }
    }

    function renderRecentActivity(attempts) {
        const container = document.getElementById(
            "dashboardRecentActivity"
        );

        if (!container) {
            return;
        }

        container.replaceChildren();

        if (!attempts.length) {
            const message = document.createElement("p");
            message.textContent = "You have no completed quizzes yet.";
            container.appendChild(message);
            return;
        }

        const attempt = attempts[0];
        const heading = document.createElement("h3");
        heading.textContent = attempt.courseName;

        const score = document.createElement("p");
        score.textContent =
            `Latest result: ${attempt.scorePercent}% (${attempt.correctCount} / ${attempt.totalQuestions} correct)`;

        const details = document.createElement("p");
        details.textContent =
            `${attempt.year} • ${attempt.timed ? "Timed quiz" : "Practice mode"}`;

        const link = document.createElement("a");
        link.href = "courses.html";
        link.className = "dashboard-small-button";
        link.textContent = "Practice Again";

        container.append(heading, score, details, link);
    }

    async function setupCourses() {
        const startButton = document.getElementById("startQuiz");

        if (!startButton) {
            return;
        }

        if (!requireAuthentication()) {
            return;
        }

        const status = document.createElement("p");
        status.id = "courseMessage";
        status.className = "api-message";
        status.setAttribute("role", "status");
        status.setAttribute("aria-live", "polite");
        startButton.before(status);

        const subjectGrid = document.querySelector(".subject-grid");
        const yearSelect = document.getElementById("yearSelect");
        const modeCards = document.querySelectorAll("[data-mode]");
        let selection;
        try {
            selection = readSelection();
        } catch (error) {
            selection = {};
            localStorage.removeItem(SELECTION_KEY);
        }
        const originalCards = new Map();

        document.querySelectorAll(
            "[data-learning-type], [data-level], [data-source]"
        ).forEach(control => {
            const group = control.closest(".selection-group");
            if (group) {
                group.style.display = "none";
            }
        });

        const paperSelect = document.getElementById("paperSelect");
        if (paperSelect) {
            const field = paperSelect.closest(".select-field");
            if (field) {
                field.style.display = "none";
            }
        }

        [
            "summaryLearningType",
            "summaryLevel",
            "summarySource",
            "summaryPaper"
        ].forEach(id => {
            const element = document.getElementById(id);
            const row = element && element.closest(".summary-item");
            if (row) {
                row.style.display = "none";
            }
        });

        document.querySelectorAll("[data-subject]").forEach(card => {
            originalCards.set(
                card.getAttribute("data-subject"),
                card
            );
        });

        const state = {
            courses: [],
            course: null,
            years: [],
            selection: {
                mode: selection.mode === "Exam Mode"
                    ? "Exam Mode"
                    : "Practice Mode",
                year: Number(selection.year) || null
            }
        };

        function updateSummary() {
            setText("summarySubject", state.course.name);
            setText("summaryYear", state.selection.year);
            setText("summaryMode", state.selection.mode);
        }

        function saveCurrentSelection() {
            saveSelection({
                courseId: state.course.id,
                subject: state.course.name,
                year: state.selection.year,
                mode: state.selection.mode
            });
        }

        async function loadYears(course) {
            status.textContent = "";
            yearSelect.disabled = true;

            try {
                const years = await API.getCourseYears(course.id);
                if (!Array.isArray(years)) {
                    throw new Error(
                        "The course-years response did not match the documented API contract."
                    );
                }

                state.years = years;
                yearSelect.replaceChildren();

                years.forEach(item => {
                    const option = document.createElement("option");
                    option.value = item.year;
                    option.textContent =
                        `${item.year} (${item.questionCount} questions)`;
                    yearSelect.appendChild(option);
                });

                if (!years.length) {
                    state.selection.year = null;
                    showMessage(status, "No question years are available for this course.");
                    yearSelect.disabled = true;
                    startButton.disabled = true;
                    return;
                }

                const requestedYear = years.find(
                    item => item.year === state.selection.year
                );
                state.selection.year = requestedYear
                    ? requestedYear.year
                    : years[0].year;
                yearSelect.value = String(state.selection.year);
                yearSelect.disabled = false;
                startButton.disabled = false;
                updateSummary();
                saveCurrentSelection();
            } catch (error) {
                showMessage(status, getErrorMessage(error));
                startButton.disabled = true;
            }
        }

        function renderCourses() {
            subjectGrid.replaceChildren();

            state.courses.forEach(course => {
                let card = originalCards.get(course.name);

                if (!card) {
                    card = document.createElement("button");
                    card.type = "button";
                    card.className = "subject-card";

                    const icon = document.createElement("span");
                    icon.className = "subject-icon";
                    icon.textContent = "📘";

                    const content = document.createElement("span");
                    const name = document.createElement("strong");
                    const count = document.createElement("small");
                    content.append(name, count);
                    card.append(icon, content);
                }

                card.dataset.subject = course.name;
                card.dataset.courseId = course.id;
                const name = card.querySelector("strong");
                const detail = card.querySelector("small");

                if (name) {
                    name.textContent = course.name;
                }

                if (detail) {
                    detail.textContent =
                        `${course.questionCount} questions available`;
                }

                card.addEventListener("click", () => {
                    state.course = course;
                    subjectGrid.querySelectorAll(".subject-card").forEach(
                        item => {
                            item.classList.toggle("active", item === card);
                        }
                    );
                    loadYears(course);
                });

                subjectGrid.appendChild(card);
            });
        }

        function setMode(mode) {
            state.selection.mode = mode;
            modeCards.forEach(card => {
                card.classList.toggle(
                    "active",
                    (card.getAttribute("data-mode") === "exam") ===
                        (mode === "Exam Mode")
                );
            });
            updateSummary();
            saveCurrentSelection();
        }

        try {
            const courses = await API.listCourses();
            if (!Array.isArray(courses)) {
                throw new Error(
                    "The courses response did not match the documented API contract."
                );
            }

            if (!courses.length) {
                showMessage(status, "There are no courses available yet.");
                startButton.disabled = true;
                return;
            }

            state.courses = courses;
            renderCourses();

            state.course = courses.find(
                course => course.id === Number(selection.courseId)
            ) || courses[0];

            subjectGrid.querySelectorAll(".subject-card").forEach(card => {
                card.classList.toggle(
                    "active",
                    Number(card.dataset.courseId) === state.course.id
                );
            });

            modeCards.forEach(card => {
                card.addEventListener("click", () => {
                    setMode(
                        card.getAttribute("data-mode") === "exam"
                            ? "Exam Mode"
                            : "Practice Mode"
                    );
                });
            });

            yearSelect.addEventListener("change", () => {
                state.selection.year = Number(yearSelect.value);
                updateSummary();
                saveCurrentSelection();
            });

            await loadYears(state.course);

            startButton.addEventListener("click", async () => {
                if (!state.course || !state.selection.year) {
                    showMessage(status, "Select a course and year first.");
                    return;
                }

                startButton.disabled = true;
                status.textContent = "";

                try {
                    const timed = state.selection.mode === "Exam Mode";
                    const request = {
                        courseId: state.course.id,
                        year: state.selection.year,
                        timed
                    };

                    if (timed) {
                        request.timeLimitSeconds = 1200;
                    }

                    const attempt = await API.startQuiz(request);
                    if (
                        !attempt ||
                        typeof attempt.attemptId !== "number" ||
                        typeof attempt.totalQuestions !== "number"
                    ) {
                        throw new Error(
                            "The quiz-start response did not match the documented API contract."
                        );
                    }

                    localStorage.setItem(
                        ATTEMPT_KEY,
                        String(attempt.attemptId)
                    );
                    localStorage.setItem(QUESTION_INDEX_KEY, "0");
                    saveCurrentSelection();
                    window.location.assign("quiz.html");
                } catch (error) {
                    showMessage(status, getErrorMessage(error));
                    startButton.disabled = false;
                }
            });

            setMode(state.selection.mode);
            updateSummary();
        } catch (error) {
            showMessage(status, getErrorMessage(error));
            startButton.disabled = true;
        }
    }

    async function setupQuiz() {
        const questionText = document.getElementById("questionText");

        if (!questionText) {
            return;
        }

        if (!requireAuthentication()) {
            return;
        }

        const attemptId = Number(localStorage.getItem(ATTEMPT_KEY));
        if (!Number.isSafeInteger(attemptId) || attemptId <= 0) {
            window.location.assign("courses.html");
            return;
        }

        const selection = readSelection();
        const timerContainer = document.getElementById(
            "quizTimerContainer"
        );
        const timerDisplay = document.getElementById("quizTimer");
        const answerOptions = document.getElementById("answerOptions");
        const answerFeedback = document.getElementById("answerFeedback");
        const previousButton = document.getElementById("previousQuestion");
        const nextButton = document.getElementById("nextQuestion");
        const navigator = document.getElementById("questionNavigator");
        const submitButton = document.getElementById("submitQuizButton");
        const submitModal = document.getElementById("submitModal");
        const submitModalText = document.getElementById("submitModalText");
        const continueButton = document.getElementById("continueQuizButton");
        const confirmSubmitButton = document.getElementById(
            "confirmSubmitButton"
        );
        const options = ["A", "B", "C", "D"];
        const answers = new Map();
        const cache = new Map();

        let attempt;
        let totalQuestions = 0;
        let currentIndex = Number(
            localStorage.getItem(QUESTION_INDEX_KEY) || 0
        );
        let timerInterval = null;
        let submitInProgress = false;
        let answerInProgress = false;
        let timedSubmitWaitingForAnswer = false;
        let questionOpenedAt = null;
        const timeSpentByIndex = new Map();

        function showQuizMessage(message) {
            showMessage(answerFeedback, message);
        }

        function stopTimer() {
            if (timerInterval !== null) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
        }

        function startTimer(remainingSeconds) {
            stopTimer();

            if (!attempt.timed || typeof remainingSeconds !== "number") {
                timerContainer.style.display = "none";
                return;
            }

            timerContainer.style.display = "block";
            let seconds = Math.max(0, remainingSeconds);

            function update() {
                const minutes = Math.floor(seconds / 60);
                const remainder = seconds % 60;
                timerDisplay.textContent =
                    `${String(minutes).padStart(2, "0")}:${String(remainder).padStart(2, "0")}`;

                if (seconds <= 0) {
                    stopTimer();
                    if (answerInProgress) {
                        timedSubmitWaitingForAnswer = true;
                    } else {
                        submitQuiz();
                    }
                    return;
                }

                seconds -= 1;
            }

            update();
            if (seconds > 0) {
                timerInterval = setInterval(update, 1000);
            }
        }

        function renderNavigator() {
            navigator.replaceChildren();

            for (let index = 0; index < totalQuestions; index += 1) {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "navigator-button";
                button.textContent = String(index + 1);

                if (index === currentIndex) {
                    button.classList.add("current");
                }

                if (answers.has(index)) {
                    button.classList.add("answered");
                }

                button.disabled = answerInProgress;
                button.addEventListener("click", () => {
                    loadQuestion(index);
                });
                navigator.appendChild(button);
            }
        }

        function renderQuestion(question) {
            const questionNumber = currentIndex + 1;
            const percentage = Math.round(
                (questionNumber / totalQuestions) * 100
            );
            const answer = answers.has(currentIndex)
                ? answers.get(currentIndex)
                : question.myAnswer;

            questionText.textContent = question.questionText;
            setText("currentQuestionNumber", questionNumber);
            setText("questionLabelNumber", questionNumber);
            setText("totalQuestions", totalQuestions);
            setText("progressPercentage", `${percentage}%`);
            document.getElementById("quizProgressFill").style.width =
                `${percentage}%`;

            answerOptions.replaceChildren();
            [
                question.optionA,
                question.optionB,
                question.optionC,
                question.optionD
            ].forEach((text, index) => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "answer-option";

                if (answer === options[index]) {
                    button.classList.add("selected");
                }

                const letter = document.createElement("span");
                letter.className = "answer-letter";
                letter.textContent = options[index];

                const label = document.createElement("span");
                label.textContent = text;
                button.append(letter, label);
                button.disabled = answerInProgress;
                button.addEventListener("click", () => {
                    selectAnswer(options[index]);
                });
                answerOptions.appendChild(button);
            });

            answerFeedback.textContent = answer
                ? "Answer saved. Correct answers are shown after submission."
                : "";
            answerFeedback.className = answer
                ? "answer-feedback show"
                : "answer-feedback";

            setText("quizSubject", attempt.courseName);
            setText(
                "quizDetails",
                `${attempt.year} • ${attempt.totalQuestions} questions`
            );
            setText(
                "quizModeBadge",
                attempt.timed ? "Exam Mode" : "Practice Mode"
            );

            previousButton.disabled =
                answerInProgress || currentIndex === 0;
            nextButton.disabled = answerInProgress;
            nextButton.textContent =
                currentIndex === totalQuestions - 1
                    ? "Review →"
                    : "Next →";

            renderNavigator();
            questionOpenedAt = Date.now();
            startTimer(question.remainingSeconds);
        }

        function elapsedOnCurrentQuestion() {
            const savedSeconds = timeSpentByIndex.get(currentIndex) || 0;
            const currentSeconds = questionOpenedAt === null
                ? 0
                : Math.floor((Date.now() - questionOpenedAt) / 1000);
            return savedSeconds + currentSeconds;
        }

        async function loadQuestion(index) {
            if (index < 0 || index >= totalQuestions) {
                return;
            }

            if (answerInProgress) {
                return;
            }

            if (questionOpenedAt !== null) {
                timeSpentByIndex.set(
                    currentIndex,
                    elapsedOnCurrentQuestion()
                );
            }
            questionOpenedAt = null;
            currentIndex = index;
            localStorage.setItem(QUESTION_INDEX_KEY, String(index));
            questionText.textContent = "Loading question…";
            answerFeedback.textContent = "";
            answerFeedback.className = "answer-feedback";
            stopTimer();

            try {
                const question = await API.getQuestion(attemptId, index);

                if (
                    !question ||
                    question.index !== index ||
                    typeof question.questionId !== "number"
                ) {
                    throw new Error(
                        "The question response did not match the documented API contract."
                    );
                }

                cache.set(index, question);
                if (question.myAnswer) {
                    answers.set(index, question.myAnswer);
                }
                renderQuestion(question);
            } catch (error) {
                showQuizMessage(getErrorMessage(error));
            }
        }

        async function selectAnswer(selectedOption) {
            if (answerInProgress) {
                return;
            }

            const question = cache.get(currentIndex);
            if (!question) {
                return;
            }

            const questionIndex = currentIndex;
            answerInProgress = true;
            previousButton.disabled = true;
            nextButton.disabled = true;
            submitButton.disabled = true;
            navigator.querySelectorAll("button").forEach(button => {
                button.disabled = true;
            });
            answerOptions.querySelectorAll("button").forEach(button => {
                button.disabled = true;
            });
            answerFeedback.textContent = "Saving answer…";

            try {
                const timeSpentSeconds = elapsedOnCurrentQuestion();
                const response = await API.saveAnswer(attemptId, {
                    questionId: question.questionId,
                    selectedOption,
                    timeSpentSeconds
                });

                if (!response || response.saved !== true) {
                    throw new Error("The answer was not saved by the API.");
                }

                answers.set(questionIndex, selectedOption);
                timeSpentByIndex.set(questionIndex, timeSpentSeconds);
                answerOptions.querySelectorAll("button").forEach(
                    (button, index) => {
                        button.classList.toggle(
                            "selected",
                            options[index] === selectedOption
                        );
                    }
                );
                answerFeedback.className = "answer-feedback show";
                answerFeedback.textContent =
                    "Answer saved. Correct answers are shown after submission.";
            } catch (error) {
                showQuizMessage(getErrorMessage(error));
            } finally {
                answerInProgress = false;
                answerOptions.querySelectorAll("button").forEach(button => {
                    button.disabled = false;
                });
                previousButton.disabled = currentIndex === 0;
                nextButton.disabled = false;
                submitButton.disabled = false;
                renderNavigator();

                if (timedSubmitWaitingForAnswer) {
                    timedSubmitWaitingForAnswer = false;
                    submitQuiz();
                }
            }
        }

        async function submitQuiz() {
            if (submitInProgress) {
                return;
            }

            submitInProgress = true;
            stopTimer();
            submitButton.disabled = true;
            confirmSubmitButton.disabled = true;

            try {
                const result = await API.submitQuiz(attemptId);
                if (!result || result.attemptId !== attemptId) {
                    throw new Error(
                        "The quiz submission response did not match the documented API contract."
                    );
                }

                window.location.assign("results.html");
            } catch (error) {
                submitInProgress = false;
                submitButton.disabled = false;
                confirmSubmitButton.disabled = false;
                showQuizMessage(getErrorMessage(error));
            }
        }

        try {
            attempt = await API.getAttempt(attemptId);
            if (
                !attempt ||
                typeof attempt.totalQuestions !== "number" ||
                typeof attempt.courseName !== "string" ||
                !["IN_PROGRESS", "COMPLETED", "EXPIRED"].includes(
                    attempt.status
                )
            ) {
                throw new Error(
                    "The quiz-attempt response did not match the documented API contract."
                );
            }

            if (
                attempt.status === "COMPLETED" ||
                attempt.status === "EXPIRED"
            ) {
                window.location.assign("results.html");
                return;
            }

            totalQuestions = attempt.totalQuestions;
            if (totalQuestions < 1) {
                throw new Error("This quiz attempt has no questions.");
            }

            if (!Number.isInteger(currentIndex) || currentIndex < 0) {
                currentIndex = 0;
            }

            currentIndex = Math.min(currentIndex, totalQuestions - 1);

            if (selection.mode !== (attempt.timed
                ? "Exam Mode"
                : "Practice Mode")) {
                saveSelection({
                    ...selection,
                    courseId: attempt.courseId,
                    subject: attempt.courseName,
                    year: attempt.year,
                    mode: attempt.timed ? "Exam Mode" : "Practice Mode"
                });
            }

            if (previousButton) {
                previousButton.addEventListener("click", () => {
                    loadQuestion(currentIndex - 1);
                });
            }

            if (nextButton) {
                nextButton.addEventListener("click", () => {
                    if (currentIndex < totalQuestions - 1) {
                        loadQuestion(currentIndex + 1);
                    } else if (submitModal) {
                        submitModalText.textContent =
                            "Review your answers or submit this quiz.";
                        submitModal.classList.add("open");
                    } else {
                        submitQuiz();
                    }
                });
            }

            if (submitButton) {
                submitButton.addEventListener("click", () => {
                    submitModalText.textContent =
                        "Submit this quiz now? Only answers saved to Quizora will be graded.";
                    submitModal.classList.add("open");
                });
            }

            if (continueButton) {
                continueButton.addEventListener("click", () => {
                    submitModal.classList.remove("open");
                });
            }

            if (confirmSubmitButton) {
                confirmSubmitButton.addEventListener("click", () => {
                    submitModal.classList.remove("open");
                    submitQuiz();
                });
            }

            await loadQuestion(currentIndex);
        } catch (error) {
            showQuizMessage(getErrorMessage(error));
        }
    }

    async function setupResults() {
        const percentageElement = document.getElementById(
            "resultPercentage"
        );

        if (!percentageElement) {
            return;
        }

        if (!requireAuthentication()) {
            return;
        }

        const attemptId = Number(localStorage.getItem(ATTEMPT_KEY));
        if (!Number.isSafeInteger(attemptId) || attemptId <= 0) {
            window.location.assign("courses.html");
            return;
        }

        try {
            const result = await API.getResult(attemptId);
            if (
                !result ||
                result.attemptId !== attemptId ||
                !Array.isArray(result.review)
            ) {
                throw new Error(
                    "The quiz-result response did not match the documented API contract."
                );
            }

            const score = Math.round(result.scorePercent);
            setText("resultPercentage", `${score}%`);
            setText("correctAnswers", result.correctCount);
            setText(
                "incorrectAnswers",
                result.answeredCount - result.correctCount
            );
            setText("resultTotalQuestions", result.totalQuestions);
            setText("resultAccuracy", `${score}%`);
            setText("resultSubject", result.courseName);
            setText("resultYear", result.year);
            setText(
                "resultMode",
                result.timed ? "Exam Mode" : "Practice Mode"
            );
            setText(
                "resultScoreText",
                `${result.correctCount} / ${result.totalQuestions}`
            );

            [
                "resultLearningType",
                "resultLevel",
                "resultSource",
                "resultPaper"
            ].forEach(id => {
                const element = document.getElementById(id);
                    const row = element && (
                        element.closest(".summary-item") ||
                        element.closest(".result-detail-item")
                    );
                    if (row) {
                        row.style.display = "none";
                    }
            });

            const message = getResultMessage(score);
            setText("resultMessage", message.title);
            setText("resultMessageText", message.text);
            renderReview(result.review);

            const retryButton = document.getElementById("retryQuizButton");
            if (retryButton) {
                retryButton.addEventListener("click", () => {
                    localStorage.removeItem(ATTEMPT_KEY);
                    localStorage.removeItem(QUESTION_INDEX_KEY);
                    window.location.assign("courses.html");
                });
            }
        } catch (error) {
            const container = document.getElementById("resultReview");
            showMessage(container, getErrorMessage(error));
        }
    }

    function renderReview(items) {
        const container = document.getElementById("resultReview");

        if (!container) {
            return;
        }

        container.replaceChildren();

        items.forEach(item => {
            const article = document.createElement("article");
            article.className = "result-review-item";

            const heading = document.createElement("h3");
            heading.textContent = `Question ${item.index + 1}`;

            const question = document.createElement("p");
            question.textContent = item.questionText;

            const choices = document.createElement("ul");
            [
                item.optionA,
                item.optionB,
                item.optionC,
                item.optionD
            ].forEach((text, index) => {
                const choice = document.createElement("li");
                const letter = ["A", "B", "C", "D"][index];
                choice.textContent = `${letter}. ${text}`;
                if (letter === item.correctOption) {
                    choice.classList.add("correct");
                }
                if (letter === item.myAnswer && !item.correct) {
                    choice.classList.add("incorrect");
                }
                choices.appendChild(choice);
            });

            const explanation = document.createElement("p");
            explanation.textContent = item.explanation || "";

            article.append(heading, question, choices, explanation);
            container.appendChild(article);
        });
    }

    window.QuizoraStudentApp = Object.freeze({
        setupLogin,
        setupAdminLogin,
        setupRegistration,
        setupDashboard,
        setupAdminDashboard,
        setupCourses,
        setupQuiz,
        setupResults
    });
})();
