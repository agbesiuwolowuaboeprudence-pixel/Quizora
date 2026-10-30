# Quizora demo integration

The existing frontend demo Mathematics questions are also seeded into the Spring Boot demo database as Mathematics **2026**. Login, quiz start, answer submission, server grading, results, and dashboard attempt counts use the backend.

## Run locally

1. Open a terminal in `backend`. On Windows run ` .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo" `.
2. Serve the repository root with VS Code Live Server. Open `login.html`.
3. Sign in with the backend demo student `student@demo.com` / `Student@123`.
4. Choose **Mathematics**, **2026**, **Objective**, and Practice or Exam Mode. Start the quiz, answer, and submit.
5. Check DevTools Network for `/api/quizzes/start`, `/questions/`, `/answers`, and `/submit`.

**Limitations:** Other subjects and years are not wired to frontend demo questions yet; they require backend content. The local demo profile uses H2 in-memory storage, so results reset on backend restart. Practice Mode explanations for the matching 2026 Mathematics demo questions remain in the frontend. The frontend still contains other local UI data, and further end-to-end browser verification is required.

No GitHub push has been performed.
