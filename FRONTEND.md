# Frontend and Spring Boot API

The frontend is a set of static HTML, CSS, and JavaScript files. The Spring Boot API remains
separate under `backend/`; this integration does not modify backend code.

## Run locally

1. Start the backend from `backend/` using its demo profile:

   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
   ```

2. Serve this repository root over HTTP with VS Code Live Server or another static-file server.
   Open `index.html` from that server, not with a `file://` URL.

The frontend defaults to `http://localhost:8080`. To use a deployed API, change the
`window.QUIZORA_API_BASE_URL` value in `api-config.js` to the API's HTTPS base URL. The API's
`CORS_ORIGINS` must also allow the exact frontend origin.

## Connected API flows

- Student and institutional registration and login use the documented `/api/auth` endpoints.
- The student dashboard and institution portal load their documented dashboard, progress,
  institution, and report-summary DTOs.
- Courses and available years come from `/api/courses`; quizzes save answers and submit through
  `/api/quizzes`. Correct answers are shown only in the result review.
- CSV and PDF institution reports download from the documented report-export endpoint.
- Subscription activation uses `POST /api/subscriptions/subscribe`. This endpoint is documented
  as a mock and activates immediately; it does not collect money or redirect to a payment provider.

The external payment-provider callback remains unimplemented until its endpoint and response
contract are confirmed.
