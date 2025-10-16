FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
COPY src/main/resources/data.sql /app/data.sql
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]