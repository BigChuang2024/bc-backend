#!/bin/bash

# Kubernetes部署脚本
# 用于将bc-backend部署到minikube

echo "🚀 开始部署bc-backend到minikube..."

# 检查minikube状态
if ! minikube status > /dev/null 2>&1; then
    echo "❌ minikube未运行，请先启动minikube"
    echo "运行命令: minikube start"
    exit 1
fi

echo "✅ minikube正在运行"

# 创建命名空间
echo "📦 创建命名空间..."
kubectl apply -f namespace.yaml

# 创建密钥（需要先配置敏感信息）
echo "🔐 创建密钥..."
echo "⚠️  请确保已编辑secrets.yaml文件并填入正确的base64编码值"
kubectl apply -f secrets.yaml

# 部署依赖服务
echo "🗄️  部署MySQL..."
kubectl apply -f mysql.yaml

echo "🗄️  部署MongoDB..."
kubectl apply -f mongodb.yaml

echo "🔴 部署Redis..."
kubectl apply -f redis.yaml

echo "🐰 部署RabbitMQ..."
kubectl apply -f rabbitmq.yaml

# 等待依赖服务就绪
echo "⏳ 等待依赖服务启动..."
kubectl wait --for=condition=ready pod -l app=mysql --timeout=300s -n bc-backend
kubectl wait --for=condition=ready pod -l app=mongodb --timeout=300s -n bc-backend
kubectl wait --for=condition=ready pod -l app=redis --timeout=300s -n bc-backend
kubectl wait --for=condition=ready pod -l app=rabbitmq --timeout=300s -n bc-backend

# 部署Spring Boot应用
echo "🚀 部署bc-backend应用..."
kubectl apply -f bc-backend.yaml

# 等待应用就绪
echo "⏳ 等待应用启动..."
kubectl wait --for=condition=ready pod -l app=bc-backend --timeout=300s -n bc-backend

# 获取服务信息
echo "📊 获取部署状态..."
kubectl get all -n bc-backend

echo ""
echo "🎉 部署完成！"
echo ""
echo "📝 下一步操作："
echo "1. 获取应用访问地址："
echo "   minikube service bc-backend -n bc-backend --url"
echo ""
echo "2. 查看应用日志："
echo "   kubectl logs -f deployment/bc-backend -n bc-backend"
echo ""
echo "3. 进入容器调试："
echo "   kubectl exec -it deployment/bc-backend -n bc-backend -- /bin/sh"
echo ""
echo "4. 删除部署："
echo "   kubectl delete -f ."
echo "   kubectl delete namespace bc-backend"