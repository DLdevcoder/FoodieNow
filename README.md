# 🍔 FoodieNow - Hệ Thống Đặt & Giao Đồ Ăn Trực Tuyến Đa Vai Trò

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-1.5.4-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Supabase](https://img.shields.io/badge/Supabase-Backend-green.svg?style=flat-square&logo=supabase)](https://supabase.com/)
[![Firebase](https://img.shields.io/badge/Firebase-FCM-orange.svg?style=flat-square&logo=firebase)](https://firebase.google.com/)
[![MapLibre](https://img.shields.io/badge/MapLibre-GL_SDK-blue.svg?style=flat-square&logo=maplibre)](https://maplibre.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

**FoodieNow** là một giải pháp ứng dụng di động đặt và giao đồ ăn toàn diện, được thiết kế chuyên biệt và tối ưu hóa cho thị trường Việt Nam. Ứng dụng tích hợp cấu trúc đa vai trò (**Khách hàng - Cửa hàng - Tài xế - Quản trị viên**) trên cùng một hệ sinh thái đồng nhất, sử dụng công nghệ Android hiện đại nhất với giao diện **Jetpack Compose** cùng hệ thống Backend thời gian thực mạnh mẽ dựa trên nền tảng **Supabase serverless**.

---

## 📸 Tổng Quan Giao Diện & Tính Năng
*Hệ thống giao diện được tối ưu hóa cao về mặt thẩm mỹ (Premium UI/UX), hỗ trợ đầy đủ cả chế độ Sáng/Tối (Light/Dark Mode) và Đa ngôn ngữ (Tiếng Việt / Tiếng Anh).*

---

## 🚀 Tính Năng Nổi Bật Theo Từng Vai Trò

### 1. Phân Hệ Khách Hàng (Customer)
*   **Trải Nghiệm Mua Sắm Cá Nhân Hóa:** Duyệt danh sách cửa hàng, món ăn nổi bật ("Must Try"), tìm kiếm thông minh và lọc danh mục món ăn nhanh chóng.
*   **Giỏ Hàng & Thanh Toán Tối Ưu:** Quản lý giỏ hàng linh hoạt từ một cửa hàng. Quy trình thanh toán tinh gọn, tích hợp đa dạng phương thức thanh toán:
    *   Thanh toán khi nhận hàng (COD).
    *   Thanh toán qua ví điện tử tích hợp hệ thống (**FoodiePay**).
    *   Hỗ trợ liên kết tài khoản thẻ và các cổng thanh toán nội địa.
*   **Khuyến Mãi & Điểm Thưởng:** Hệ thống tích điểm thưởng sau mỗi đơn hàng thành công, đổi điểm để giảm giá trực tiếp, và áp dụng mã giảm giá (Voucher) của hệ thống hoặc cửa hàng.
*   **Bản Đồ Giao Hàng Thời Gian Thực:** Định vị địa chỉ giao hàng bằng GPS, hiển thị bản đồ trực quan và theo dõi vị trí của tài xế di chuyển theo thời gian thực (Real-time Tracking).
*   **Hệ Thống Đánh Giá (Reviews):** Phản hồi chất lượng dịch vụ thông qua đánh giá số sao (Rating) và bình luận trực quan trên từng món ăn.
*   **Trò Chuyện Trực Tiếp (Real-time Chat):** Chat trực tiếp với Cửa hàng hoặc Tài xế giao hàng để trao đổi chi tiết đơn hàng thông qua kết nối thời gian thực.

### 2. Phân Hệ Cửa Hàng (Merchant)
*   **Quản Lý Thực Đơn (Menu Management):** Thêm mới, chỉnh sửa món ăn, cập nhật trạng thái còn/hết hàng trực quan. Hỗ trợ chụp và tải hình ảnh món ăn trực tiếp lên bộ lưu trữ đám mây (Supabase Storage).
*   **Xử Lý Đơn Hàng Tự Động:** Tiếp nhận đơn hàng mới lập tức nhờ cơ chế Real-time Database. Cập nhật tiến độ chuẩn bị món ăn (`PENDING` -> `PREPARING` -> `DELIVERING`).
*   **Thống Kê Doanh Thu & Phản Hồi:** Xem báo cáo doanh thu nhanh chóng, quản lý các đánh giá và phản hồi của khách hàng để nâng cao chất lượng dịch vụ.
*   **Quản Lý Cửa Hàng:** Tùy chỉnh trạng thái hoạt động, cập nhật thời gian đóng/mở cửa, thông tin liên lạc và địa chỉ của quán.

### 3. Phân Hệ Tài Xế (Shipper)
*   **Bản Đồ Chỉ Đường Chuyên Nghiệp:** Sử dụng thư viện bản đồ **MapLibre GL** kết hợp nền tảng bản đồ số Việt Nam **Goong Maps**, hiển thị lộ trình di chuyển tối ưu nhất từ vị trí hiện tại -> Cửa hàng -> Khách hàng.
*   **Nhận & Giao Đơn Hàng:** Nhận đơn hàng trống trong khu vực gần nhất, cập nhật nhanh trạng thái giao hàng bằng một chạm.
*   **Quản Lý Doanh Thu:** Thống kê chi tiết thu nhập theo ngày, tuần, tháng và lịch sử giao hàng đã hoàn thành.

### 4. Phân Hệ Quản Trị Viên (Admin)
*   **Bảng Điều Khiển Tổng Quan (Dashboard):** Thống kê tổng doanh thu toàn hệ thống, số lượng tài khoản đăng ký mới, số đơn hàng đang xử lý và biểu đồ tăng trưởng doanh số.
*   **Quản Lý Hệ Thống:** Duyệt/khóa tài khoản người dùng, cấu hình tham số hệ thống, giám sát giao dịch rút tiền và theo dõi các chỉ số vận hành thời gian thực.

---

## 🛠️ Công Nghệ Sử Dụng (Technology Stack)

### Frontend (Android App)
*   **Language:** Kotlin (100%) - Tối ưu mã nguồn sạch và hiệu năng xử lý.
*   **UI Framework:** **Jetpack Compose** kết hợp Material Design 3 đem lại giao diện mượt mà, hiện đại và chuẩn Premium UI.
*   **Dependency Injection:** **Dagger Hilt** hỗ trợ quản lý các luồng phụ thuộc rõ ràng, dễ bảo trì và kiểm thử.
*   **Networking:** **Ktor Client (OkHttp engine)** xử lý các kết nối API gọn nhẹ và tối ưu tốc độ.
*   **Local Caching:** **Room Database** giúp lưu trữ đệm dữ liệu ngoại tuyến và **DataStore Preferences** quản lý cấu hình người dùng (ngôn ngữ, giao diện sáng/tối).
*   **Image Loading:** **Coil (v3)** hỗ trợ tải và hiển thị hình ảnh từ URL nhanh chóng, tiết kiệm băng thông.
*   **Async Processing:** **Kotlin Coroutines & Flow** quản lý các tác vụ bất đồng bộ cực kỳ mượt mà.

### Backend & Cloud Services
*   **Database & Auth:** **Supabase**
    *   **PostgreSQL:** Cơ sở dữ liệu quan hệ mạnh mẽ, chuẩn hóa cao.
    *   **GoTrue Auth:** Hệ thống xác thực người dùng an toàn, hỗ trợ quản lý phân quyền chi tiết (RLS - Row Level Security).
    *   **Postgrest-kt & Realtime-kt:** Hỗ trợ kết nối trực tiếp từ thiết bị di động đến database với cơ chế đăng ký sự kiện thời gian thực (Chat, Tracking đơn hàng).
    *   **Supabase Storage:** Lưu trữ hình ảnh món ăn và ảnh đại diện người dùng chất lượng cao.
    *   **Serverless Edge Functions:** Xử lý các tác vụ nền như gửi thông báo đẩy qua Firebase.
*   **Notification Service:** **Firebase Cloud Messaging (FCM)** gửi thông báo đẩy thời gian thực về thiết bị khi có thay đổi trạng thái đơn hàng hoặc tin nhắn mới.
*   **Map API:** **Goong Maps SDK / MapLibre GL** tối ưu bản đồ số tại Việt Nam để tính toán lộ trình giao hàng chuẩn xác.

---

## 🏛️ Kiến Trúc Hệ Thống (Clean Architecture)

Dự án tuân thủ chặt chẽ cấu trúc **Clean Architecture** chia nhỏ thành các tầng độc lập để tối đa hóa khả năng bảo trì (Maintainability) và kiểm thử (Testability):

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
    ├── cart / payment   # Giỏ hàng, xử lý thanh toán ví FoodiePay, COD
    ├── chat             # Trò chuyện thời gian thực
    ├── merchant         # Giao diện dành riêng cho chủ cửa hàng
    ├── shipper          # Giao diện dành riêng cho đối tác vận chuyển
    └── admin            # Bảng điều khiển quản trị hệ thống
```

---

## 💾 Kiến Trúc Database & Logic Trực Quan (Supabase SQL)

Hệ thống sử dụng các tính năng cao cấp của PostgreSQL trên Supabase để thực hiện các xử lý nghiệp vụ phức tạp trực tiếp tại Database nhằm đảm bảo tính toàn vẹn dữ liệu cực kỳ chính xác (Atomic Transactions):

1.  **Hàm thanh toán nguyên tử (`process_payment`):**
    *   Xác thực khách hàng và kiểm tra tính hợp lệ của giỏ hàng.
    *   Áp dụng các điều kiện giảm giá của **Voucher** hệ thống hoặc cửa hàng.
    *   Sử dụng điểm thưởng để giảm giá đơn hàng nếu có yêu cầu.
    *   Tự động trừ số dư ví **FoodiePay** và lưu lịch sử giao dịch wallet đồng nhất.
    *   Tính toán cộng điểm thưởng mới cho khách hàng dựa trên hóa đơn thực tế.
    *   Tự động lưu bảng dữ liệu `orders`, `order_items`, `payments` và `voucher_usages` trong một khối giao dịch duy nhất. Nếu bất kỳ bước nào lỗi, toàn bộ tiến trình sẽ được khôi phục (Rollback) tự động.
2.  **Hệ Thống Triggers Tự Động:**
    *   `update_store_rating()`: Tự động tính toán điểm trung bình đánh giá (AVG Rating) của cửa hàng mỗi khi khách hàng thêm hoặc xóa đánh giá món ăn.
    *   `trigger_order_status_notification()`: Tự động gửi bản tin thông báo vào bảng `notifications` khi trạng thái đơn hàng cập nhật (đang làm món, đang giao, hoàn thành hoặc hủy).
    *   `trigger_wallet_transaction_notification()`: Tự động thông báo giao dịch ví khi có biến động số dư.

---

## ⚙️ Hướng Dẫn Cài Đặt & Chạy Dự Án

### 📋 Yêu Cầu Hệ Thống
*   **Android Studio** phiên bản Koala (2024.1.1) trở lên.
*   **JDK 11** trở lên.
*   Thiết bị Android vật lý hoặc Giả lập có **SDK 26 (Android 8.0)** trở lên.
*   Đã cài đặt **Supabase CLI** (nếu muốn triển khai cơ sở dữ liệu cục bộ).

### 🛠️ Các Bước Thực Hiện

#### Bước 1: Clone dự án
```bash
git clone https://github.com/DLdevcoder/FoodieNow.git
cd FoodieNow
```

#### Bước 2: Thiết lập khóa API Bản đồ (Goong Maps)
Mở file `local.properties` ở thư mục gốc của dự án (nếu chưa có, hãy tạo mới) và thêm cấu hình các khóa API của bạn từ [Goong.io](https://goong.io/):
```properties
GOONG_API_KEY=your_goong_api_key_here
GOONG_MAPTILES_KEY=your_goong_maptiles_key_here
```

#### Bước 3: Triển khai Cấu trúc Database Supabase
1. Đăng nhập vào trang quản trị [Supabase Dashboard](https://supabase.com/) và tạo một dự án mới.
2. Truy cập mục **SQL Editor** và lần lượt thực thi các file SQL cấu trúc cơ sở dữ liệu có sẵn tại thư mục:
   `supabase/migrations/schema/` (Thực thi từ `001_tables.sql` đến `011_system_settings.sql`).
3. (Tùy chọn) Chạy file dữ liệu mẫu có tại: `supabase/migrations/seeds/` để có dữ liệu thử nghiệm ban đầu.

#### Bước 4: Cấu hình khóa Supabase trên Ứng dụng
Tạo hoặc cập nhật cấu hình endpoint và API key của Supabase trong file cấu hình mạng của dự án tại `com/example/foodienow/core/network/NetworkModule.kt` hoặc khai báo thông qua cấu hình môi trường tương ứng để ứng dụng khởi tạo SDK chính xác.

#### Bước 5: Cấu hình thông báo đẩy (Firebase Cloud Messaging)
1. Truy cập [Firebase Console](https://console.firebase.google.com/), tạo một dự án mới và liên kết với Package Name: `com.example.foodienow`.
2. Tải xuống file cấu hình `google-services.json` và lưu vào thư mục dự án tại đường dẫn: `app/google-services.json`.
3. Triển khai Supabase Edge Function gửi thông báo đẩy tại thư mục `supabase/functions/push-notification` lên Supabase để tự động kích hoạt thông báo khi có bản ghi thông báo mới được thêm vào database.

#### Bước 6: Đồng bộ hóa và Chạy ứng dụng
1. Mở dự án trong **Android Studio**.
2. Thực hiện **Sync Project with Gradle Files** để tải tất cả các thư viện cần thiết.
3. Nhấp nút **Run** (biểu tượng tam giác xanh) để biên dịch và cài đặt ứng dụng lên thiết bị của bạn.

---

## 📜 Giấy Phép (License)
Dự án được phân phối dưới giấy phép **MIT License**. Chi tiết xem tại tệp tin `LICENSE` đi kèm trong mã nguồn.

---

*Chúc bạn có những trải nghiệm tuyệt vời cùng **FoodieNow**! Mọi thắc mắc hoặc yêu cầu hỗ trợ phát triển vui lòng mở Issue hoặc liên hệ trực tiếp qua Email nhà phát triển.* 😉
