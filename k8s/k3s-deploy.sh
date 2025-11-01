#!/bin/bash

# Kubernetes部署脚本 - 适配k3s
# 用于将bc-backend部署到k3s集群

echo "🚀 开始部署bc-backend到k3s集群..."

# 检查kubectl是否连接
if ! kubectl cluster-info > /dev/null 2>&1; then
    echo "❌ kubectl未连接到k3s集群，请检查k3s状态"
    echo "运行命令: sudo systemctl status k3s"
    exit 1
fi

echo "✅ k3s集群连接正常"

# 检查当前上下文
CURRENT_CONTEXT=$(kubectl config current-context)
echo "📋 当前Kubernetes上下文: $CURRENT_CONTEXT"

# 创建命名空间
echo "📦 创建命名空间..."
kubectl apply -f namespace.yaml

# 创建密钥（需要先配置敏感信息）
echo "🔐 创建密钥..."
echo "⚠️  请确保已编辑secrets.yaml文件并填入正确的base64编码值"
read -p "是否继续部署密钥？(y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    kubectl apply -f secrets.yaml
else
    echo "跳过密钥部署，请手动运行: kubectl apply -f secrets.yaml"
fi

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

# 获取NodePort访问地址
echo ""
echo "🌐 获取服务访问信息..."
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
NODE_PORT=$(kubectl get service bc-backend -n bc-backend -o jsonpath='{.spec.ports[0].nodePort}')

echo ""
echo "🎉 部署完成！"
echo ""
echo "📝 访问信息："
echo "   应用地址: http://$NODE_IP:$NODE_PORT"
echo ""
echo "🔧 常用命令："
echo "1. 查看应用日志："
echo "   kubectl logs -f deployment/bc-backend -n bc-backend"
echo ""
echo "2. 进入容器调试："
echo "   kubectl exec -it deployment/bc-backend -n bc-backend -- /bin/sh"
echo ""
echo "3. 查看所有资源："
echo "   kubectl get all -n bc-backend"
echo ""
echo "4. 删除部署："
echo "   kubectl delete -f ."
echo "   kubectl delete namespace bc-backend"
echo ""
echo "5. 查看Ingress（如果配置了）："
echo "   kubectl get ingress -n bc-backend"