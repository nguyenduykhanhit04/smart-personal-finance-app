FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR đã build sẵn từ target
COPY target/finance-backend-*.jar app.jar

# Copy các file cần thiết khi runtime
COPY src/main/resources/firebase-service-account.json ./firebase-service-account.json
COPY src/main/resources/random_forest_model.onnx      ./random_forest_model.onnx
COPY src/main/resources/ai-settings.properties        ./ai-settings.properties

# Thư mục lưu file upload avatar/hình ảnh giao dịch
RUN mkdir -p uploads logs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
