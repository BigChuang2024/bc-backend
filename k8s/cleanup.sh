#!/bin/bash

# Kubernetes清理脚本
# 用于清理bc-backend部署

echo "🧹 开始清理bc-backend部署..."

# 删除所有资源
echo "🗑️  删除所有Kubernetes资源..."
kubectl delete -f bc-backend.yaml
kubectl delete -f rabbitmq.yaml
kubectl delete -f redis.yaml
kubectl delete -f mongodb.yaml
kubectl delete -f mysql.yaml
kubectl delete -f secrets.yaml

# 等待资源删除完成
echo "⏳ 等待资源删除完成..."
sleep 10

# 删除命名空间
echo "🗑️  删除命名空间..."
kubectl delete -f namespace.yaml

# 检查是否还有残留资源
echo "🔍 检查残留资源..."
kubectl get all -n bc-backend 2>/dev/null && echo "⚠️  仍有资源存在，请手动检查" || echo "✅ 所有资源已清理完成"

echo ""
echo "🎉 清理完成！"
echo ""
echo "📝 如果需要重新部署，请运行："
echo "   ./deploy.sh"