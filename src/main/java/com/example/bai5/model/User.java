package com.example.bai5.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Authentication =====
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // ===== Basic Info =====
    private String fullName;

    // ===== Status =====
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // ===== Relationship =====
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserVoucher> userVouchers;

    // ===== Audit =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = UserStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}