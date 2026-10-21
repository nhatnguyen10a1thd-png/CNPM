# =========================================================================
# GIAI ĐOẠN 1: BUILD JAR BẰNG MAVEN (Java 21)
# =========================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy file định nghĩa dependency và toàn bộ mã nguồn
COPY pom.xml .
COPY src ./src

# Đóng gói file .jar (bỏ qua unit test để build nhanh và tiết kiệm tài nguyên server)
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# =========================================================================
# GIAI ĐOẠN 2: RUNTIME SIÊU NHẸ (JRE 21 Alpine ~ 180MB)
# =========================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tạo user bảo mật không có quyền root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Lấy file jar đã đóng gói từ giai đoạn 1
COPY --from=builder /build/target/*.jar app.jar

# =========================================================================
# BÍ QUYẾT CHỐNG TRÀN RAM (OOM - Exit Code 137) TRÊN GÓI 512MB CỦA RENDER:
# - -Xmx350m: Giới hạn bộ nhớ Heap tối đa 350MB (chừa 162MB cho OS & JRE)
# - -Xms128m: Khởi động nhẹ với 128MB
# - -Xss512k: Giảm dung lượng stack mỗi luồng từ 1MB xuống 512KB để tiết kiệm RAM
# - -XX:+UseSerialGC: Bộ dọn rác ít tốn RAM CPU nhất cho môi trường container nhỏ
# =========================================================================
ENV JAVA_OPTS="-Xmx350m -Xms128m -Xss512k -XX:+UseSerialGC -Dfile.encoding=UTF-8"
ENV PORT=8080

EXPOSE 8080

# Chạy ứng dụng và tự động lắng nghe cổng do Render cấp
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
