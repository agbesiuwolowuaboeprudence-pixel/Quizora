package com.quizora.backend.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A bulk licence purchased by an institution (e.g. 20 seats).
 * Students onboard with the generated code until seats run out or the licence expires.
 */
@Entity
@Table(name = "license_codes")
public class LicenseCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int usedSeats;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LicenseStatus status = LicenseStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public LicenseCode() {
    }

    public LicenseCode(String code, Institution institution, int totalSeats,
                       LocalDate validFrom, LocalDate validUntil) {
        this.code = code;
        this.institution = institution;
        this.totalSeats = totalSeats;
        this.usedSeats = 0;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = LicenseStatus.ACTIVE;
    }

    /** True when the code can still accept a new student today. */
    public boolean isUsable() {
        LocalDate today = LocalDate.now();
        return status == LicenseStatus.ACTIVE
                && usedSeats < totalSeats
                && !today.isBefore(validFrom)
                && !today.isAfter(validUntil);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getUsedSeats() { return usedSeats; }
    public void setUsedSeats(int usedSeats) { this.usedSeats = usedSeats; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
