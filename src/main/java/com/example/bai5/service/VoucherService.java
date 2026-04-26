package com.example.bai5.service;

import com.example.bai5.model.*;
import com.example.bai5.repository.UserRepository;
import com.example.bai5.repository.UserVoucherRepository;
import com.example.bai5.repository.VoucherRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    @Transactional
    public void applyVoucher(Long userId, String code) {

        try {
            // ===== 1. LOCK VOUCHER =====
            Voucher voucher = voucherRepository.findByCodeForUpdate(code)
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

            LocalDateTime now = LocalDateTime.now();

            // ===== 2. VALIDATION =====
            if (voucher.getStatus() != VoucherStatus.ACTIVE) {
                throw new RuntimeException("Voucher đã bị vô hiệu hóa");
            }

            if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
                throw new RuntimeException("Voucher chưa đến thời gian sử dụng");
            }

            if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
                throw new RuntimeException("Voucher đã hết hạn");
            }

            if (voucher.getUsedCount() >= voucher.getQuantity()) {
                throw new RuntimeException("Voucher đã hết lượt sử dụng");
            }

            boolean alreadyUsed = userVoucherRepository
                    .existsByUserIdAndVoucherId(userId, voucher.getId());

            if (alreadyUsed) {
                throw new RuntimeException("Bạn đã sử dụng voucher này rồi");
            }

            // ===== 3. LOAD USER (REAL ENTITY) =====
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // ===== 4. APPLY (CRITICAL SECTION) =====
            voucher.setUsedCount(voucher.getUsedCount() + 1);

            UserVoucher uv = new UserVoucher();
            uv.setUser(user);
            uv.setVoucher(voucher);
            uv.setUsedAt(now);

            // Lưu user_voucher trước → để DB constraint bắt duplicate
            userVoucherRepository.saveAndFlush(uv);

            // Sau đó update voucher
            voucherRepository.saveAndFlush(voucher);

        } catch (DataIntegrityViolationException e) {
            // unique(user_id, voucher_id)
            throw new RuntimeException("Bạn đã sử dụng voucher này rồi");

        } catch (LockTimeoutException | JpaSystemException e) {
            // lock bị timeout / deadlock
            throw new RuntimeException("Hệ thống đang bận, vui lòng thử lại");

        } catch (Exception e) {
            throw e; // giữ nguyên message business
        }
    }
}