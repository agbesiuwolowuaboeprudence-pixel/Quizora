
const QUIZORA_API_URL = "http://localhost:8080/api";

const QuizoraAPI = {
    async request(endpoint, options = {}) {
        const token = sessionStorage.getItem("quizoraToken");

        const headers = {
            "Content-Type": "application/json",
            ...options.headers
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        let response;

        try {
            response = await fetch(
                `${QUIZORA_API_URL}${endpoint}`,
                {
                    ...options,
                    headers
                }
            );
        } catch {
            throw new Error(
                "Cannot connect to Quizora server. Check that the backend is running."
            );
        }

        if (!response.ok) {
            if (response.status === 401) {
                throw new Error("Invalid login details or session expired.");
            }

            if (response.status === 403) {
                throw new Error("Access denied.");
            }

            throw new Error(
                `Request failed. Server returned ${response.status}.`
            );
        }

        return response.status === 204
            ? null
            : response.json();
    },

    login(email, password) {
        return this.request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
    },

    register(fullName, email, password) {
        return this.request("/auth/register", {
            method: "POST",
            body: JSON.stringify({ fullName, email, password })
        });
    },

    registerInstitutional(code, fullName, email, password) {
        return this.request("/auth/register-institutional", {
            method: "POST",
            body: JSON.stringify({
                code,
                fullName,
                email,
                password
            })
        });
    },

    getCourses() { return this.request("/courses"); },
    startQuiz(data) { return this.request("/quizzes/start", { method: "POST", body: JSON.stringify(data) }); },
    getQuizQuestion(id, index) { return this.request(`/quizzes/${id}/questions/${index}`); },
    saveQuizAnswer(id, data) { return this.request(`/quizzes/${id}/answers`, { method: "POST", body: JSON.stringify(data) }); },
    submitQuiz(id) { return this.request(`/quizzes/${id}/submit`, { method: "POST" }); },
    getQuizResult(id) { return this.request(`/quizzes/${id}/result`); },
    getProgress() { return this.request("/progress"); },

    getProfile() {
        return this.request("/auth/me");
    },

    saveSession(data) {
        if (!data || !data.token || !data.user) {
            throw new Error("Invalid authentication response.");
        }

        sessionStorage.setItem("quizoraToken", data.token);

        localStorage.setItem(
            "quizoraStudentName",
            data.user.fullName
        );

        localStorage.setItem(
            "quizoraStudentEmail",
            data.user.email
        );
    },

    logout() {
        sessionStorage.removeItem("quizoraToken");
        localStorage.removeItem("quizoraStudentName");
        localStorage.removeItem("quizoraStudentEmail");
    }
};
