# ============================================================
# Stage 1: Build JAR bằng Maven (build environment)
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy pom.xml trước để cache Maven dependencies
COPY pom.xml .
RUN apk add --no-cache maven && mvn dependency:go-offline -q

# Copy source code và build
COPY src ./src
RUN mvn package -DskipTests -q

# ============================================================
# Stage 2: Chạy app (runtime environment - nhỏ hơn nhiều)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR từ stage build
COPY --from=builder /app/target/finance-backend-*.jar app.jar

# Copy các file cần thiết khi runtime
COPY src/main/resources/firebase-service-account.json ./firebase-service-account.json
COPY src/main/resources/random_forest_model.onnx      ./random_forest_model.onnx
COPY src/main/resources/ai-settings.properties        ./ai-settings.properties

# Thư mục lưu file upload avatar/hình ảnh giao dịch
RUN mkdir -p uploads logs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
