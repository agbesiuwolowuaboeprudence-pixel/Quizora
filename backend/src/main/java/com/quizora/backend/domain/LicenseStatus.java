package com.quizora.backend.domain;

public enum LicenseStatus {
    ACTIVE,
    EXHAUSTED,   // all seats used
    EXPIRED,     // past validUntil
    REVOKED
}
