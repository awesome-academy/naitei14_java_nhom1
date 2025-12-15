## 📊 Monthly Statistics Scheduler - Example Output

### 1. Chatwork Message Format (CẬP NHẬT)

Khi scheduler chạy (hoặc khi test qua `/admin/test-monthly-stats`), message sẽ được gửi đến Chatwork với format như sau:

```
┌────────────────────────────────────────────────┐
│ 📊 Monthly Statistics Report - 12/2025        │
├────────────────────────────────────────────────┤
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━                    │
│ 💰 Total Revenue: $150,500.00                  │
│    (COMPLETED ORDERS ONLY)                     │
│ 📦 Total Orders: 1,234 (Completed: 980)       │
│ 🛍️ Total Products: 89                          │
│ 👥 Total Users: 567                            │
│                                                │
│ 🏆 TOP SELLING PRODUCTS:                       │
│    1. Premium Burger - 234 sold ($11,700.00)  │
│    2. French Fries - 189 sold ($3,780.00)     │
│    3. Cola Drink - 156 sold ($2,340.00)       │
│    4. Chicken Wings - 145 sold ($7,250.00)    │
│    5. Pizza Slice - 123 sold ($4,920.00)      │
│                                                │
│ ⭐ TOP CUSTOMERS:                               │
│    1. John Doe - 45 orders ($2,250.00)        │
│    2. Jane Smith - 38 orders ($1,900.00)      │
│    3. Bob Johnson - 32 orders ($1,600.00)     │
│    4. Alice Brown - 28 orders ($1,400.00)     │
│    5. Charlie Wilson - 25 orders ($1,250.00)  │
│                                                │
│ ━━━━━━━━━━━━━━━━━━━━━━━━━━                    │
│ Generated: 31/12/2025 23:00:00                 │
└────────────────────────────────────────────────┘
```

### 2. Console Log Output

```
2025-12-31 23:00:00.123  INFO 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Starting monthly statistics scheduler...
2025-12-31 23:00:00.456  INFO 12345 --- [scheduling-1] o.e.f.service.impl.ChatworkServiceImpl    : Monthly statistics sent to Chatwork for 12/2025
2025-12-31 23:00:00.789  INFO 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Monthly statistics sent successfully for 12/2025
```

### 3. Manual Test via Browser

**URL**: `http://localhost:8080/admin/test-monthly-stats`

**Response**:
```
Monthly statistics sent to Chatwork! Check your room.
```

**Browser Display**:
```
Monthly statistics sent to Chatwork! Check your room.
```

### 4. Sample Data Scenarios

#### Scenario A: New E-commerce Site (Low Volume)
```
📊 Monthly Statistics Report - 01/2025
━━━━━━━━━━━━━━━━━━━━━━━━━━
💰 Total Revenue: $2,450.00
📦 Total Orders: 15
🛍️ Total Products: 25
👥 Total Users: 45
━━━━━━━━━━━━━━━━━━━━━━━━━━
Generated: 31/01/2025 23:00:00
```

#### Scenario B: Established Store (High Volume)
```
📊 Monthly Statistics Report - 12/2025
━━━━━━━━━━━━━━━━━━━━━━━━━━
💰 Total Revenue: $1,254,780.50
📦 Total Orders: 8,456
🛍️ Total Products: 432
👥 Total Users: 12,890
━━━━━━━━━━━━━━━━━━━━━━━━━━
Generated: 31/12/2025 23:00:00
```

#### Scenario C: Holiday Season Peak
```
📊 Monthly Statistics Report - 12/2024
━━━━━━━━━━━━━━━━━━━━━━━━━━
💰 Total Revenue: $3,567,234.75
📦 Total Orders: 24,567
🛍️ Total Products: 567
👥 Total Users: 45,678
━━━━━━━━━━━━━━━━━━━━━━━━━━
Generated: 31/12/2024 23:00:00
```

### 5. API Response Example

When calling the test endpoint:

**Request**:
```http
GET /admin/test-monthly-stats HTTP/1.1
Host: localhost:8080
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Monthly statistics sent to Chatwork! Check your room.
```

### 6. Chatwork API Request (Internal)

The actual API request sent to Chatwork:

**Endpoint**: `https://api.chatwork.com/v2/rooms/417678000/messages`

**Headers**:
```
X-ChatWorkToken: 702f1d4d6b1a74b15fdc20d9ed8ae49e
Content-Type: application/x-www-form-urlencoded
```

**Body** (form-urlencoded):
```
body=[info][title]📊 Monthly Statistics Report - 12/2025[/title]━━━━━━━━━━━━━━━━━━━━━━━━━━%0A💰 Total Revenue: $150,500.00%0A📦 Total Orders: 1,234%0A🛍️ Total Products: 89%0A👥 Total Users: 567%0A━━━━━━━━━━━━━━━━━━━━━━━━━━%0AGenerated: 31/12/2025 23:00:00[/info]
```

### 7. Error Scenarios

#### Error 1: Chatwork API Failure
```
2025-12-31 23:00:00.123  INFO 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Starting monthly statistics scheduler...
2025-12-31 23:00:01.456  ERROR 12345 --- [scheduling-1] o.e.f.service.impl.ChatworkServiceImpl    : Failed to send monthly statistics to Chatwork
org.springframework.web.client.HttpClientErrorException$Unauthorized: 401 Unauthorized
    at org.springframework.web.client.HttpClientErrorException.create(HttpClientErrorException.java:105)
    ...
2025-12-31 23:00:01.789  ERROR 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Error occurred while sending monthly statistics
```

#### Error 2: Database Connection Issue
```
2025-12-31 23:00:00.123  INFO 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Starting monthly statistics scheduler...
2025-12-31 23:00:00.456  ERROR 12345 --- [scheduling-1] o.e.f.scheduler.MonthlyStatisticsScheduler : Error occurred while sending monthly statistics
java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available
    ...
```

### 8. Timeline of Execution

```
Day 31 (Last day of month)
├── 22:59:59 - Application running normally
├── 23:00:00 - Scheduler triggered by cron
│   ├── 23:00:00.001 - Log: "Starting monthly statistics scheduler..."
│   ├── 23:00:00.050 - Fetch dashboard statistics from database
│   ├── 23:00:00.100 - Format message with current month/year
│   ├── 23:00:00.200 - Send HTTP POST to Chatwork API
│   ├── 23:00:00.500 - Receive response from Chatwork
│   └── 23:00:00.501 - Log: "Monthly statistics sent successfully..."
└── 23:00:01 - Return to normal operation

Day 1 (First day of next month)
└── No scheduler runs (waits until next month's last day)
```

### 9. Comparison: Before vs After

**Before** (Manual Process):
1. Admin logs in at month end
2. Opens dashboard
3. Takes screenshot or writes down numbers
4. Manually composes message
5. Posts to Chatwork manually
6. ⏱️ Time: ~5-10 minutes
7. ❌ Risk: Forget to do it, human error

**After** (Automated):
1. ✅ Automatic execution at 23:00
2. ✅ Consistent format
3. ✅ Never forgets
4. ✅ Real-time data
5. ✅ Professional looking
6. ⏱️ Time: 0 minutes (automated)
7. ✅ Reliable and accurate

### 10. Integration with Other Systems

The scheduler can be extended to:

```java
// Send to multiple channels
chatworkService.sendMonthlyStatistics(stats, month, year);
slackService.sendMonthlyStatistics(stats, month, year);
emailService.sendMonthlyReport(stats, month, year);

// Generate PDF report
byte[] pdfReport = reportService.generateMonthlyPDF(stats);
fileService.saveReport(pdfReport, month, year);

// Store in database
reportRepository.save(new MonthlyReport(stats, month, year));
```

---

## 🎯 Key Benefits Demonstrated

1. **Automation**: Zero manual intervention required
2. **Consistency**: Same format every time
3. **Reliability**: Runs automatically, never forgets
4. **Professional**: Clean, formatted output
5. **Extensible**: Easy to add more notifications
6. **Testable**: Simple test endpoint for verification
7. **Monitored**: Full logging for tracking
8. **Error-Resilient**: Continues working even if one notification fails
