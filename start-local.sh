#!/bin/bash

# 本地开发环境启动脚本

echo "🚀 启动 bc-backend 本地开发环境..."

# 检查 .env.properties 文件是否存在
if [ ! -f "src/main/resources/.env.properties" ]; then
    echo "❌ 未找到 .env.properties 文件"
    echo "请先复制 .env.properties.example 为 src/main/resources/.env.properties 并配置您的环境变量"
    exit 1
fi

# 检查依赖服务是否运行
echo "🔍 检查依赖服务..."
if ! docker compose ps | grep -q "Up"; then
    echo "⚠️  依赖服务未运行，正在启动..."
    docker compose up -d
    echo "⏳ 等待服务启动..."
    sleep 10
fi

# 启动应用
echo "🎯 启动 Spring Boot 应用..."
mvn spring-boot:run -Dspring.profiles.active=local

echo "✅ 应用已启动！访问 http://localhost:8080"