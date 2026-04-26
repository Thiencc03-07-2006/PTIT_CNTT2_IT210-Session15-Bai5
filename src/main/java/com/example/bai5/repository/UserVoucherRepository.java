package com.example.bai5.repository;

import com.example.bai5.model.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
}