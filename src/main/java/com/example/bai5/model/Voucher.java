package com.example.bai5.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    private int quantity;
    private int usedCount;

    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}