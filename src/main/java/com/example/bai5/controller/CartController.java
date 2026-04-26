package com.example.bai5.controller;

import com.example.bai5.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final VoucherService voucherService;

    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String code,
                               Principal principal,
                               Model model) {

        try {
            Long userId = Long.parseLong(principal.getName());

            voucherService.applyVoucher(userId, code);

            model.addAttribute("success", "Áp dụng voucher thành công!");

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
        }

        return "cart";
    }
}