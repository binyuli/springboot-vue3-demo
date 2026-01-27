# 第一阶段：使用 Maven 构建项目
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn package -DskipTests

# 第二阶段：使用 JRE 运行应用（精简镜像）
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/*.jar app.jar

# JVM 内存优化配置（总内存限制约 200MB）
ENV JAVA_OPTS="-Xmx200m -Xms100m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UnlockExperimentalVMOptions"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
