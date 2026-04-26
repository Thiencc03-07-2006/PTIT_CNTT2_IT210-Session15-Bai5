package com.example.bai5.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_voucher",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "voucher_id"}))
@Getter
@Setter
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Voucher voucher;

    private LocalDateTime usedAt;
}