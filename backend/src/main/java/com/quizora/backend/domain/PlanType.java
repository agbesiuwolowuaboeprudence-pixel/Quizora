package com.quizora.backend.domain;

public enum PlanType {
    TRIAL,          // free trial given on individual sign-up
    MONTHLY,        // paid individual plan (Paystack/Mobile Money in a later phase)
    YEARLY,         // paid individual plan
    INSTITUTIONAL   // covered by the institution's bulk licence
}
