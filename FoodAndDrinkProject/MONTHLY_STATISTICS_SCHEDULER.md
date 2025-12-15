# 📊 Monthly Statistics Scheduler - Hướng dẫn sử dụng

## Tổng quan
Tính năng tự động gửi báo cáo thống kê hàng tháng tới Chatwork vào cuối mỗi tháng.

## 🎯 Chức năng

### 1. Tự động gửi thống kê cuối tháng
- **Thời gian chạy**: Ngày cuối cùng của mỗi tháng lúc 23:00 (11:00 PM)
- **Nội dung**: 
  - 💰 Tổng doanh thu (CHỈ từ orders COMPLETED)
  - 📦 Tổng số đơn hàng (Total + Completed)
  - 🛍️ Tổng số sản phẩm (Total Products)
  - 👥 Tổng số người dùng (Total Users)
  - 🏆 Top 5 sản phẩm bán chạy nhất
  - ⭐ Top 5 khách hàng VIP (mua nhiều nhất)

### 2. Cấu hình Cron Expression
```java
@Scheduled(cron = "0 0 23 L * ?")
```
- `0`: Giây 0
- `0`: Phút 0
- `23`: Giờ 23 (11 PM)
- `L`: Ngày cuối cùng của tháng (Last day)
- `*`: Mọi tháng
- `?`: Bất kỳ ngày nào trong tuần

## 📁 Các file đã tạo/sửa đổi

### 1. New Files:
- [`MonthlyStatisticsScheduler.java`](src/main/java/org/example/foodanddrinkproject/scheduler/MonthlyStatisticsScheduler.java)
  - Scheduler component tự động chạy cuối tháng

### 2. Modified Files:
- [`ChatworkService.java`](src/main/java/org/example/foodanddrinkproject/service/ChatworkService.java)
  - Thêm method `sendMonthlyStatistics()`
  
- [`ChatworkServiceImpl.java`](src/main/java/org/example/foodanddrinkproject/service/impl/ChatworkServiceImpl.java)
  - Implement logic gửi thống kê với format đẹp
  
- [`FoodAndDrinkProjectApplication.java`](src/main/java/org/example/foodanddrinkproject/FoodAndDrinkProjectApplication.java)
  - Thêm `@EnableScheduling`
  
- [`AdminWebController.java`](src/main/java/org/example/foodanddrinkproject/controller/admin/web/AdminWebController.java)
  - Thêm endpoint test `/admin/test-monthly-stats`
  
- [`application.properties`](src/main/resources/application.properties)
  - Thêm cấu hình scheduler

## 🧪 Cách test

### 1. Test thủ công qua API
```bash
# Truy cập URL sau để test ngay:
http://localhost:8080/admin/test-monthly-stats
```

### 2. Test programmatically
```java
@Autowired
private MonthlyStatisticsScheduler scheduler;

// Gọi method test
scheduler.sendStatisticsManually();
```

### 3. Xem log
```
[MonthlyStatisticsScheduler] Starting monthly statistics scheduler...
[MonthlyStatisticsScheduler] Monthly statistics sent successfully for 12/2025
[ChatworkServiceImpl] Monthly statistics sent to Chatwork for 12/2025
```

## 📊 Format message gửi tới Chatwork

```
📊 Monthly Statistics Report - 12/2025
━━━━━━━━━━━━━━━━━━━━━━━━━━
💰 Total Revenue: $150,500.00
📦 Total Orders: 1,234
🛍️ Total Products: 89
👥 Total Users: 567
━━━━━━━━━━━━━━━━━━━━━━━━━━
Generated: 31/12/2025 23:00:00
```

## ⚙️ Cấu hình trong application.properties

```properties
# Enable scheduling
spring.task.scheduling.enabled=true

# Thread pool size for scheduled tasks
spring.task.scheduling.pool.size=2
```

## 🔧 Tùy chỉnh thời gian chạy

### Thay đổi thời gian trong MonthlyStatisticsScheduler.java:

```java
// Chạy mỗi ngày cuối tháng lúc 23:00
@Scheduled(cron = "0 0 23 L * ?")

// Chạy ngày đầu tháng lúc 00:00 (để báo cáo tháng trước)
@Scheduled(cron = "0 0 0 1 * ?")

// Chạy mỗi thứ 2 đầu tiên của tháng lúc 09:00
@Scheduled(cron = "0 0 9 ? * MON#1")

// Test: Chạy mỗi phút (để test nhanh)
@Scheduled(cron = "0 * * * * ?")

// Test: Chạy mỗi 5 phút
@Scheduled(cron = "0 */5 * * * ?")
```

## 📋 Cron Expression Reference

| Expression | Ý nghĩa |
|------------|---------|
| `0 0 23 L * ?` | Cuối tháng lúc 23:00 |
| `0 0 0 1 * ?` | Đầu tháng lúc 00:00 |
| `0 0 9 * * MON` | Mỗi thứ 2 lúc 09:00 |
| `0 0 */6 * * ?` | Mỗi 6 giờ |
| `0 0 0 * * ?` | Mỗi ngày lúc 00:00 |

## 🔍 Troubleshooting

### 1. Scheduler không chạy
- ✅ Kiểm tra `@EnableScheduling` trong main class
- ✅ Kiểm tra `spring.task.scheduling.enabled=true`
- ✅ Xem log có thông báo scheduler start không

### 2. Không nhận được message trên Chatwork
- ✅ Kiểm tra Chatwork API token trong `application-local.properties`
- ✅ Kiểm tra Room ID đúng chưa
- ✅ Xem log có lỗi gì không
- ✅ Test bằng `/admin/test-chatwork` trước

### 3. Thời gian chạy không đúng
- ✅ Kiểm tra timezone của server
- ✅ Có thể cần set timezone:
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Ho_Chi_Minh
```

## 🚀 Dependencies

Tất cả dependencies cần thiết đã có sẵn trong project:
- Spring Boot Starter (scheduling support built-in)
- Spring Web (RestTemplate)
- Lombok
- SLF4J Logger

## 📝 Notes

1. **Security**: API token không nên commit vào Git. Sử dụng environment variables trong production.

2. **Performance**: Scheduler chạy trong thread pool riêng, không ảnh hưởng đến main application.

3. **Error Handling**: Nếu có lỗi khi gửi, sẽ log error nhưng không crash application.

4. **Manual Trigger**: Có thể trigger thủ công qua endpoint hoặc gọi method `sendStatisticsManually()`.

## 🎉 Kết quả

Sau khi implement xong, hệ thống sẽ:
- ✅ Tự động gửi báo cáo cuối mỗi tháng
- ✅ Format message đẹp, dễ đọc
- ✅ Log đầy đủ để tracking
- ✅ Có endpoint test để kiểm tra
- ✅ Error handling tốt, không crash app
