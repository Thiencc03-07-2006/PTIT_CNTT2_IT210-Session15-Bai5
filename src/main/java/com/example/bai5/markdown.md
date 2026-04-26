# PHẦN A — THIẾT KẾ KIẾN TRÚC

## 1. Thiết kế cơ sở dữ liệu (ERD)

### Các bảng chính

**Bảng Voucher**
* `id` (PK): Khóa chính.
* `code` (Unique): Mã giảm giá duy nhất.
* `discount_value`: Giá trị giảm giá.
* `quantity`: Tổng số lượt sử dụng tối đa.
* `used_count`: Số lượt đã sử dụng thực tế.
* `status`: Trạng thái (ACTIVE, INACTIVE).
* `start_date`, `end_date`: Thời hạn sử dụng.
* `version`: Sử dụng cho cơ chế Optimistic Lock.

**Bảng User**
* `id` (PK): Khóa chính.
* `email`: Địa chỉ email người dùng.

**Bảng UserVoucher (Bảng trung gian)**
* `id` (PK): Khóa chính.
* `user_id` (FK): Liên kết tới người dùng.
* `voucher_id` (FK): Liên kết tới voucher.
* `used_at`: Thời điểm sử dụng.

**Ràng buộc kỹ thuật (Constraint):**
* `UNIQUE(user_id, voucher_id)`: Đảm bảo quy tắc nghiệp vụ mỗi người dùng chỉ được sử dụng một loại voucher tối đa một lần.

---

## 2. Phân tích lỗi vượt quá số lượng (Race Condition)

**Kịch bản xảy ra lỗi:**
1.  **Luồng A:** Đọc dữ liệu thấy `used_count = 9` (còn 1 lượt trống).
2.  **Luồng B:** Cùng thời điểm đọc dữ liệu thấy `used_count = 9`.
3.  Cả hai luồng đều xác nhận điều kiện hợp lệ để thực hiện giao dịch.
4.  **Luồng A:** Cập nhật dữ liệu lên 10.
5.  **Luồng B:** Cập nhật dữ liệu lên 11 (Vượt quá giới hạn cho phép).

**Nguyên nhân gốc rễ:** Hệ thống thiếu cơ chế khóa (Locking) hoặc kiểm soát ghi đồng thời (Concurrent Write) tại thời điểm dữ liệu được thay đổi.

---

## 3. Chiến lược xử lý Concurrency

### Phương pháp 1: Optimistic Locking (@Version)
* **Cơ chế:** Mỗi dòng dữ liệu đính kèm một số phiên bản (version). Khi thực hiện cập nhật, hệ thống kiểm tra xem version hiện tại có khớp với lúc đọc hay không. Nếu bị luồng khác thay đổi trước đó, hệ thống ném ngoại lệ `OptimisticLockException`.
* **Ưu điểm:** Hiệu năng cao, không khóa tài nguyên cơ sở dữ liệu, khả năng mở rộng (Scale) tốt.
* **Nhược điểm:** Khi số lượng yêu cầu tranh chấp cao, tỉ lệ thất bại và nhu cầu thử lại (retry) tăng mạnh.

### Phương pháp 2: Pessimistic Locking (FOR UPDATE)
* **Cơ chế:** Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` để khóa dòng dữ liệu ngay từ thời điểm đọc. Các luồng khác phải chờ cho đến khi luồng hiện tại hoàn tất giao dịch (Commit/Rollback).
* **Ưu điểm:** Ngăn chặn tuyệt đối tình trạng bán quá số lượng (Oversell), đảm bảo tính chính xác cao nhất.
* **Nhược điểm:** Giảm băng thông xử lý của hệ thống (Throughput), dễ gây ra hiện tượng thắt nút cổ chai (Bottleneck).

---

## 4. Quyết định kỹ thuật

Đối với kịch bản **Flash Sale** (Tranh chấp cực cao trong thời gian ngắn), giải pháp được lựa chọn là **PESSIMISTIC LOCK**.

**Lý do:**
1.  Ưu tiên tính chính xác tuyệt đối của dữ liệu (Không chấp nhận Oversell).
2.  Trong giai đoạn cao điểm, việc sử dụng Optimistic Lock sẽ gây ra lượng lớn ngoại lệ và yêu cầu Retry liên tục, dẫn đến nguy cơ quá tải và treo hệ thống.

---