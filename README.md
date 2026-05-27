# 🍔 FoodieNow - Hệ Thống Đặt & Giao Đồ Ăn Trực Tuyến Đa Vai Trò

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-1.5.4-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-Backend-green.svg?style=flat-square&logo=supabase)](https://supabase.com/)
[![Firebase](https://img.shields.io/badge/Firebase-FCM-orange.svg?style=flat-square&logo=firebase)](https://firebase.google.com/)
[![MapLibre](https://img.shields.io/badge/MapLibre-GL_SDK-blue.svg?style=flat-square&logo=maplibre)](https://maplibre.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

**FoodieNow** là hệ thống ứng dụng di động đặt và giao đồ ăn tích hợp bốn vai trò (**Khách hàng - Cửa hàng - Tài xế - Quản trị viên**) trên cùng một hệ sinh thái. Dự án sử dụng Jetpack Compose cho giao diện người dùng và nền tảng Supabase làm hệ thống backend thời gian thực.

---

## 📸 Tổng Quan Giao Diện & Tính Năng
*Hệ thống giao diện hỗ trợ chế độ Sáng/Tối (Light/Dark Mode) và Đa ngôn ngữ (Tiếng Việt / Tiếng Anh).*

---

## 🚀 Tính Năng Theo Từng Vai Trò

### 1. Phân Hệ Khách Hàng (Customer)
*   **Duyệt và Tìm Kiếm:** Duyệt danh sách cửa hàng, món ăn gợi ý, tìm kiếm và lọc món ăn theo danh mục.
*   **Giỏ Hàng & Đặt Hàng:** Quản lý giỏ hàng từ một cửa hàng. Phương thức hoàn tất đơn hàng hỗ trợ:
    *   Nhận hàng trả tiền (COD).
    *   Sử dụng số dư tài khoản nội bộ (**FoodiePay**).
    *   Liên kết tài khoản thẻ và các cổng giao dịch.
*   **Khuyến Mãi & Điểm Thưởng:** Tích lũy điểm thưởng sau mỗi đơn hàng hoàn thành, dùng điểm thưởng để giảm giá đơn hàng và áp dụng mã giảm giá (Voucher).
*   **Bản Đồ Giao Hàng:** Định vị địa chỉ bằng GPS, hiển thị bản đồ lộ trình và theo dõi vị trí di chuyển của tài xế theo thời gian thực.
*   **Đánh Giá (Reviews):** Gửi đánh giá số sao và bình luận cho các món ăn đã đặt.
*   **Trò Chuyện Trực Tiếp (Real-time Chat):** Nhắn tin trực tiếp với Cửa hàng hoặc Tài xế trong quá trình giao dịch.

### 2. Phân Hệ Cửa Hàng (Merchant)
*   **Quản Lý Thực Đơn:** Thêm mới, chỉnh sửa món ăn, cập nhật trạng thái hoạt động của món ăn. Tải hình ảnh món ăn lên Supabase Storage.
*   **Xử Lý Đơn Hàng:** Tiếp nhận đơn hàng mới và cập nhật trạng thái xử lý (`PENDING` -> `PREPARING` -> `DELIVERING`).
*   **Thống Kê Doanh Thu & Phản Hồi:** Theo dõi báo cáo doanh thu và quản lý các đánh giá của khách hàng.
*   **Quản Lý Thông Tin:** Thiết lập trạng thái hoạt động, thời gian đóng/mở cửa và thông tin liên hệ của cửa hàng.

### 3. Phân Hệ Tài Xế (Shipper)
*   **Bản Đồ Chỉ Đường:** Sử dụng thư viện bản đồ MapLibre GL và Goong Maps để hiển thị lộ trình di chuyển giữa các điểm: Vị trí tài xế -> Cửa hàng -> Khách hàng.
*   **Nhận & Giao Đơn Hàng:** Nhận các đơn hàng trong khu vực lân cận và cập nhật trạng thái giao nhận đơn hàng.
*   **Quản Lý Doanh Thu:** Thống kê thu nhập cá nhân theo ngày, tuần, tháng và lịch sử các chuyến giao hàng.

### 4. Phân Hệ Quản Trị Viên (Admin)
*   **Bảng Điều Khiển Tổng Quan (Dashboard):** Xem thống kê tổng doanh thu, số lượng tài khoản đăng ký mới, số đơn hàng đang xử lý.
*   **Quản Lý Hệ Thống:** Kích hoạt hoặc tạm khóa tài khoản người dùng, cấu hình tham số hệ thống, giám sát giao dịch rút tiền và các chỉ số vận hành.

---

## 🛠️ Công Nghệ Sử Dụng (Technology Stack)

### Frontend (Android App)
*   **Language:** Kotlin (100%).
*   **UI Framework:** Jetpack Compose kết hợp Material Design 3.
*   **Dependency Injection:** Dagger Hilt hỗ trợ quản lý các luồng phụ thuộc.
*   **Networking:** Ktor Client (OkHttp engine) kết nối các API hệ thống.
*   **Local Caching:** Room Database lưu trữ dữ liệu ngoại tuyến và DataStore Preferences lưu trữ cấu hình thiết lập người dùng (ngôn ngữ, giao diện).
*   **Image Loading:** Coil (v3) hỗ trợ tải và hiển thị hình ảnh từ URL.
*   **Async Processing:** Kotlin Coroutines & Flow quản lý các tác vụ bất đồng bộ.

### Backend & Cloud Services
*   **Database & Auth:** Supabase
    *   **PostgreSQL:** Cơ sở dữ liệu quan hệ, thiết kế chuẩn hóa.
    *   **GoTrue Auth:** Quản lý đăng ký, đăng nhập và phân quyền truy cập dữ liệu (RLS - Row Level Security).
    *   **Postgrest-kt & Realtime-kt:** Kết nối dữ liệu và đăng ký sự kiện thay đổi trạng thái theo thời gian thực.
    *   **Supabase Storage:** Lưu trữ tệp tin hình ảnh món ăn và ảnh đại diện.
    *   **Serverless Edge Functions:** Thực thi các tác vụ nền như kích hoạt thông báo gửi sang Firebase.
*   **Notification Service:** Firebase Cloud Messaging (FCM) gửi thông báo đẩy đến thiết bị khi có trạng thái đơn hàng mới hoặc tin nhắn mới.
*   **Map API:** Goong Maps SDK / MapLibre GL tính toán lộ trình giao hàng tại Việt Nam.

---

## 🏛️ Kiến Trúc Hệ Thống (Clean Architecture)

Dự án áp dụng cấu trúc Clean Architecture chia nhỏ thành các tầng độc lập để đảm bảo khả năng bảo trì và kiểm thử:

```text
com.example.foodienow
│
├── core                 # Tầng dùng chung của toàn ứng dụng
│   ├── designsystem     # Theme (Màu sắc, Typography), components tùy chỉnh
│   ├── di               # Định nghĩa Hilt Module cung cấp tài nguyên hệ thống
│   ├── navigation       # Quản lý luồng điều hướng màn hình (AppNavigation, Screen)
│   └── network          # Cấu hình Ktor client kết nối Supabase
│
├── data                 # Thực thi việc lưu trữ và truy vấn dữ liệu
│   ├── local            # Room Database, DataStore Preferences
│   ├── remote           # Lớp kết nối API, lấy dữ liệu từ Supabase và Firebase
│   └── repository       # Triển khai các Interface Repository từ domain
│
├── domain               # Lớp chứa Logic nghiệp vụ cốt lõi (Không phụ thuộc Framework)
│   ├── model            # Định nghĩa các Data Class thực thể (Food, Order, User...)
│   └── repository       # Định nghĩa các Interface quy ước truy xuất dữ liệu
│
└── feature              # Chứa các màn hình hiển thị trực quan theo từng tính năng
    ├── auth             # Đăng ký, đăng nhập, khôi phục mật khẩu, xác thực
    ├── customer_home    # Màn hình chính khách hàng, tìm kiếm
    ├── food_detail      # Chi tiết món ăn, đánh giá
    ├── cart / order     # Giỏ hàng, xử lý đơn hàng qua ví FoodiePay, COD
    ├── chat             # Trò chuyện thời gian thực
    ├── merchant         # Giao diện dành riêng cho chủ cửa hàng
    ├── shipper          # Giao diện dành riêng cho đối tác vận chuyển
    └── admin            # Bảng điều khiển quản trị hệ thống
```

---

## 💾 Kiến Trúc Database & Logic (Supabase SQL)

Hệ thống sử dụng các tính năng của PostgreSQL trên Supabase để thực hiện xử lý nghiệp vụ trực tiếp tại Database nhằm đảm bảo tính toàn vẹn dữ liệu (Atomic Transactions):

1.  **Xác thực và xử lý dữ liệu (`process_payment`):**
    *   Xác thực định danh người dùng và đối chiếu quyền truy cập giỏ hàng.
    *   Kiểm tra và xử lý các điều kiện áp dụng mã giảm giá (Voucher).
    *   Tính toán và khấu trừ điểm thưởng tích lũy theo yêu cầu của người dùng.
    *   Khấu trừ số dư ví điện tử nội bộ (**FoodiePay**) và ghi nhận lịch sử giao dịch tương ứng.
    *   Tính toán điểm thưởng được cộng thêm sau khi hoàn thành giao dịch dựa trên tổng giá trị hóa đơn.
    *   Ghi nhận thông tin đồng bộ vào các bảng dữ liệu `orders`, `order_items`, `payments` và `voucher_usages` trong cùng một giao dịch (Transaction). Nếu có lỗi xảy ra trong quá trình, hệ thống tự động thực hiện khôi phục trạng thái (Rollback).
2.  **Hệ Thống Triggers Tự Động:**
    *   `update_store_rating()`: Tự động tính toán điểm trung bình đánh giá (AVG Rating) của cửa hàng khi có thay đổi (thêm hoặc xóa) đánh giá món ăn.
    *   `trigger_order_status_notification()`: Tự động thêm bản ghi thông báo vào bảng `notifications` khi trạng thái đơn hàng thay đổi (đang làm món, đang giao, hoàn thành hoặc hủy).
    *   `trigger_wallet_transaction_notification()`: Tự động thêm bản ghi thông báo giao dịch khi có biến động số dư ví điện tử.

---

## ⚙️ Hướng Dẫn Cài Đặt & Chạy Dự Án

### 📋 Yêu Cầu Hệ Thống
*   Android Studio phiên bản Koala (2024.1.1) trở lên.
*   JDK 11 trở lên.
*   Thiết bị Android vật lý hoặc Giả lập hỗ trợ SDK từ phiên bản 26 (Android 8.0) trở lên.
*   Đã cài đặt Supabase CLI (nếu muốn chạy cơ sở dữ liệu cục bộ).

### 🛠️ Các Bước Thực Hiện

#### Bước 1: Clone dự án
```bash
git clone https://github.com/DLdevcoder/FoodieNow.git
cd FoodieNow
```

#### Bước 2: Thiết lập khóa API Bản đồ (Goong Maps)
Tạo mới hoặc mở file `local.properties` tại thư mục gốc của dự án và khai báo cấu hình các khóa API từ Goong.io:
```properties
GOONG_API_KEY=your_goong_api_key_here
GOONG_MAPTILES_KEY=your_goong_maptiles_key_here
```

#### Bước 3: Triển khai Cấu trúc Database Supabase
1. Tạo một dự án mới trên trang quản lý Supabase Dashboard.
2. Sử dụng công cụ SQL Editor để thực thi tuần tự các file SQL trong thư mục:
   `supabase/migrations/schema/` (thực thi từ `001_tables.sql` đến `011_system_settings.sql`).
3. (Tùy chọn) Chạy tệp SQL dữ liệu mẫu tại `supabase/migrations/seeds/` để tạo các bản ghi thử nghiệm ban đầu.

#### Bước 4: Cấu hình khóa Supabase trên Ứng dụng
Thiết lập URL endpoint và API key của Supabase trong file cấu hình tại `com/example/foodienow/core/network/NetworkModule.kt` hoặc thông qua các biến môi trường để SDK khởi tạo kết nối.

#### Bước 5: Cấu hình thông báo đẩy (Firebase Cloud Messaging)
1. Tạo dự án mới trên Firebase Console và đăng ký ứng dụng với Package Name: `com.example.foodienow`.
2. Tải xuống tệp tin cấu hình `google-services.json` và sao chép vào thư mục `app/` của dự án.
3. Triển khai mã nguồn Supabase Edge Function tại thư mục `supabase/functions/push-notification` lên nền tảng Supabase để thực hiện gửi thông báo qua FCM khi bảng dữ liệu thông báo ghi nhận bản ghi mới.

#### Bước 6: Đồng bộ hóa và Chạy ứng dụng
1. Mở thư mục dự án bằng công cụ Android Studio.
2. Thực hiện đồng bộ hóa dự án (Sync Project with Gradle Files).
3. Nhấp chọn biểu tượng biên dịch và chạy (Run) để cài đặt ứng dụng lên thiết bị hoặc giả lập.

---

## 📜 Giấy Phép (License)
Dự án được phân phối dưới giấy phép MIT License. Chi tiết xem tại tệp tin `LICENSE` đi kèm trong mã nguồn.

---

*Mọi thắc mắc hoặc yêu cầu hỗ trợ phát triển vui lòng mở Issue hoặc liên hệ trực tiếp qua Email của nhà phát triển.*
