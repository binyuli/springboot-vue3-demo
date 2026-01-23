# 第一阶段：使用Maven构建项目
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 构建项目，跳过测试
RUN mvn package -DskipTests

# 第二阶段：使用OpenJDK运行应用
FROM eclipse-temurin:17-jdk

# 设置工作目录
WORKDIR /app

# 安装curl，用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=default

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]