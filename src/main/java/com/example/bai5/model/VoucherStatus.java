package com.example.bai5.model;

public enum VoucherStatus {
    ACTIVE,     // Có thể sử dụng
    INACTIVE,   // Bị tắt thủ công bởi admin
    EXPIRED,    // Hết hạn theo thời gian
    USED_UP     // Đã dùng hết số lượng
}