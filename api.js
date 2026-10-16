(function () {
    const API_BASE_URL = (
        window.QUIZORA_API_BASE_URL ||
        "http://localhost:8080"
    ).replace(/\/+$/, "");

    const TOKEN_KEY = "quizoraToken";
    const NAME_KEY = "quizoraStudentName";
    const EMAIL_KEY = "quizoraStudentEmail";
    const ROLE_KEY = "quizoraRole";
    const INSTITUTION_ID_KEY = "quizoraInstitutionId";
    const INSTITUTION_NAME_KEY = "quizoraInstitutionName";

    class ApiError extends Error {
        constructor(message, status) {
            super(message);
            this.name = "ApiError";
            this.status = status;
        }
    }

    function getValue(key) {
        return sessionStorage.getItem(key) ||
            localStorage.getItem(key);
    }

    function getAuthToken() {
        return getValue(TOKEN_KEY);
    }

    function getAuthName() {
        return getValue(NAME_KEY);
    }

    function getAuthEmail() {
        return getValue(EMAIL_KEY);
    }

    function getAuthRole() {
        return getValue(ROLE_KEY);
    }

    function getInstitutionId() {
        const value = getValue(INSTITUTION_ID_KEY);
        return value ? Number(value) : null;
    }

    function clearAuthSession() {
        [sessionStorage, localStorage].forEach(storage => {
            storage.removeItem(TOKEN_KEY);
            storage.removeItem(NAME_KEY);
            storage.removeItem(EMAIL_KEY);
            storage.removeItem(ROLE_KEY);
            storage.removeItem(INSTITUTION_ID_KEY);
            storage.removeItem(INSTITUTION_NAME_KEY);
        });
    }

    function setAuthSession(response, rememberMe) {
        if (
            !response ||
            typeof response.token !== "string" ||
            !response.user ||
            typeof response.user.fullName !== "string" ||
            typeof response.user.email !== "string" ||
            typeof response.user.role !== "string"
        ) {
            throw new Error(
                "The authentication response did not match the documented API contract."
            );
        }

        clearAuthSession();

        const storage = rememberMe
            ? localStorage
            : sessionStorage;

        storage.setItem(TOKEN_KEY, response.token);
        storage.setItem(NAME_KEY, response.user.fullName);
        storage.setItem(EMAIL_KEY, response.user.email);
        storage.setItem(ROLE_KEY, response.user.role);

        if (Number.isSafeInteger(response.user.institutionId)) {
            storage.setItem(
                INSTITUTION_ID_KEY,
                String(response.user.institutionId)
            );
        }

        if (response.user.institutionName) {
            storage.setItem(
                INSTITUTION_NAME_KEY,
                response.user.institutionName
            );
        }
    }

    async function request(path, options) {
        const requestOptions = options || {};
        const headers = new Headers({
            Accept: "application/json"
        });

        if (requestOptions.body !== undefined) {
            headers.set("Content-Type", "application/json");
        }

        const token = requestOptions.publicRequest
            ? null
            : getAuthToken();

        if (token) {
            headers.set("Authorization", `Bearer ${token}`);
        }

        const response = await fetch(
            `${API_BASE_URL}${path}`,
            {
                method: requestOptions.method || "GET",
                headers,
                body: requestOptions.body === undefined
                    ? undefined
                    : JSON.stringify(requestOptions.body)
            }
        );

        const responseText = await response.text();

        if (!response.ok) {
            if (
                response.status === 401 &&
                !requestOptions.publicRequest
            ) {
                clearAuthSession();
                window.location.assign("login.html");
            }

            throw new ApiError(
                responseText ||
                    `The API request failed with status ${response.status}.`,
                response.status
            );
        }

        if (!responseText) {
            return null;
        }

        return JSON.parse(responseText);
    }

    async function downloadReport(institutionId, format) {
        const response = await fetch(
            `${API_BASE_URL}/api/institutions/${encodeURIComponent(institutionId)}/report/export?format=${encodeURIComponent(format)}`,
            {
                headers: {
                    Authorization: `Bearer ${getAuthToken()}`
                }
            }
        );

        if (!response.ok) {
            const responseText = await response.text();
            if (response.status === 401) {
                clearAuthSession();
                window.location.assign("admin-login.html");
            }
            throw new ApiError(
                responseText ||
                    `The report request failed with status ${response.status}.`,
                response.status
            );
        }

        const disposition = response.headers.get("Content-Disposition") || "";
        const filename = disposition.match(/filename="?([^";]+)"?/i);
        const blobUrl = URL.createObjectURL(await response.blob());
        const link = document.createElement("a");
        link.href = blobUrl;
        link.download = filename
            ? filename[1]
            : `quizora-report.${format}`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(blobUrl), 1000);
    }

    window.QuizoraApi = Object.freeze({
        ApiError,
        getAuthToken,
        getAuthName,
        getAuthEmail,
        getAuthRole,
        getInstitutionId,
        isAuthenticated: () => Boolean(getAuthToken()),
        setAuthSession,
        clearAuthSession,
        login: credentials => request(
            "/api/auth/login",
            {
                method: "POST",
                body: credentials,
                publicRequest: true
            }
        ),
        register: details => request(
            "/api/auth/register",
            {
                method: "POST",
                body: details,
                publicRequest: true
            }
        ),
        registerInstitutional: details => request(
            "/api/auth/register-institutional",
            {
                method: "POST",
                body: details,
                publicRequest: true
            }
        ),
        getDashboard: () => request("/api/dashboard"),
        getProgress: () => request("/api/progress"),
        getInstitution: institutionId => request(
            `/api/institutions/${encodeURIComponent(institutionId)}`
        ),
        getInstitutionSummary: institutionId => request(
            `/api/institutions/${encodeURIComponent(institutionId)}/report/summary`
        ),
        downloadInstitutionReport: downloadReport,
        listCourses: () => request("/api/courses"),
        getCourseYears: courseId => request(
            `/api/courses/${encodeURIComponent(courseId)}/years`
        ),
        startQuiz: details => request(
            "/api/quizzes/start",
            {
                method: "POST",
                body: details
            }
        ),
        getAttempt: attemptId => request(
            `/api/quizzes/${encodeURIComponent(attemptId)}`
        ),
        getQuestion: (attemptId, index) => request(
            `/api/quizzes/${encodeURIComponent(attemptId)}/questions/${index}`
        ),
        saveAnswer: (attemptId, answer) => request(
            `/api/quizzes/${encodeURIComponent(attemptId)}/answers`,
            {
                method: "POST",
                body: answer
            }
        ),
        submitQuiz: attemptId => request(
            `/api/quizzes/${encodeURIComponent(attemptId)}/submit`,
            {
                method: "POST"
            }
        ),
        getResult: attemptId => request(
            `/api/quizzes/${encodeURIComponent(attemptId)}/result`
        ),
        subscribe: details => request(
            "/api/subscriptions/subscribe",
            {
                method: "POST",
                body: details
            }
        )
    });
})();
