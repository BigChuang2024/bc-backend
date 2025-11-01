# K3s 部署指南

## 🎯 部署概览

本指南将帮助你将 Spring Boot 应用部署到 k3s 集群。你的项目已经配置了完整的 Kubernetes 资源，只需要进行少量适配即可在 k3s 上运行。

## 📋 前置条件

1. **k3s 集群已安装并运行**
2. **kubectl 已配置连接到 k3s**
3. **项目镜像已推送到 GitHub Packages**
4. **敏感信息已准备**

### 检查 k3s 状态
```bash
# 检查 k3s 服务状态
sudo systemctl status k3s

# 检查 kubectl 连接
kubectl cluster-info

# 检查节点状态
kubectl get nodes
```

## 🔧 部署步骤

### 1. 准备敏感信息

编辑 `k8s/secrets.yaml` 文件，填入 base64 编码的敏感信息：

```bash
# 生成 base64 编码值
echo -n "your-deepseek-api-key" | base64
echo -n "your-openai-api-key" | base64
echo -n "your-jwt-secret" | base64
echo -n "your-oss-access-key-id" | base64
echo -n "your-oss-access-key-secret" | base64
```

将生成的 base64 字符串填入对应的字段。

### 2. 执行部署

使用专门为 k3s 准备的部署脚本：

```bash
# 给脚本执行权限
chmod +x k8s/k3s-deploy.sh

# 执行部署
./k8s/k3s-deploy.sh
```

### 3. 验证部署

```bash
# 查看所有资源状态
kubectl get all -n bc-backend

# 查看应用日志
kubectl logs -f deployment/bc-backend -n bc-backend

# 检查服务状态
kubectl describe service bc-backend -n bc-backend
```

## 🌐 访问应用

部署完成后，应用将通过 NodePort 暴露：

```bash
# 获取访问地址
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
NODE_PORT=$(kubectl get service bc-backend -n bc-backend -o jsonpath='{.spec.ports[0].nodePort}')
echo "应用地址: http://$NODE_IP:$NODE_PORT"
```

或者使用端口转发：
```bash
kubectl port-forward service/bc-backend 8080:8080 -n bc-backend
```
然后访问 `http://localhost:8080`

## 🏗️ 架构说明

### 部署的资源

- **命名空间**: `bc-backend`
- **数据库服务**:
  - MySQL (StatefulSet)
  - MongoDB (Deployment)
  - Redis (Deployment)
  - RabbitMQ (Deployment)
- **应用服务**:
  - bc-backend (Deployment + Service)

### 网络架构

```
外部访问 → NodePort (k3s) → bc-backend Service → bc-backend Pod
                                 ↓
                    MySQL/MongoDB/Redis/RabbitMQ Services
```

### k3s 特定说明

- **内置负载均衡**: k3s 内置 Traefik 作为 Ingress 控制器
- **轻量级存储**: 使用 local-path-provisioner 提供动态存储
- **服务发现**: 通过 CoreDNS 提供内部 DNS 解析

## 配置说明

### 环境变量配置

应用通过环境变量获取配置：

- **数据库连接**：使用Kubernetes服务发现
- **AI服务**：从Secrets获取API密钥
- **应用配置**：通过环境变量覆盖默认值

### 健康检查

应用配置了健康检查：

- **Liveness Probe**：检测应用是否存活
- **Readiness Probe**：检测应用是否就绪
- 使用Spring Boot Actuator的/health端点

## 故障排除

### 常见问题

1. **Pod无法启动**
   ```bash
   # 查看Pod详情
   kubectl describe pod <pod-name> -n bc-backend

   # 查看Pod日志
   kubectl logs <pod-name> -n bc-backend
   ```

2. **服务无法连接**
   ```bash
   # 检查服务端点
   kubectl get endpoints -n bc-backend

   # 测试网络连接
   kubectl exec -it deployment/bc-backend -n bc-backend -- curl mysql:3306
   ```

3. **镜像拉取失败**
   ```bash
   # 检查镜像拉取密钥
   kubectl get secrets -n bc-backend

   # 手动拉取镜像测试
   docker pull ghcr.io/bigchuang2024/bc-backend:latest
   ```

### 调试命令

```bash
# 进入容器调试
kubectl exec -it deployment/bc-backend -n bc-backend -- /bin/sh

# 查看事件
kubectl get events -n bc-backend --sort-by=.metadata.creationTimestamp

# 查看资源使用情况
kubectl top pods -n bc-backend
```

## 清理部署

```bash
# 删除所有资源
kubectl delete -f . -n bc-backend

# 删除命名空间
kubectl delete namespace bc-backend

# 或者使用脚本
./cleanup.sh
```

## 生产环境建议

1. **使用外部数据库**：生产环境建议使用云数据库服务
2. **配置持久化存储**：确保数据持久化到云存储
3. **设置资源限制**：根据实际使用情况调整资源请求和限制
4. **配置监控告警**：集成Prometheus和Grafana进行监控
5. **使用HTTPS**：配置TLS证书
6. **备份策略**：定期备份数据库

## 扩展部署

### 水平扩展
```bash
# 扩展应用副本数
kubectl scale deployment/bc-backend --replicas=3 -n bc-backend
```

### 使用Ingress
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: bc-backend-ingress
  namespace: bc-backend
spec:
  rules:
  - host: bc-backend.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: bc-backend
            port:
              number: 8080
```

## 🎓 学习要点

通过这个部署过程，你学到了：

1. **Docker Compose 到 Kubernetes 的转换**
2. **Kubernetes 核心概念：Deployment、Service、StatefulSet**
3. **k3s 网络和服务发现机制**
4. **敏感信息管理（Secrets）**
5. **持久化存储配置**
6. **健康检查和资源限制**

这个配置为你的项目提供了生产级别的部署能力！

## 🔄 持续部署

### 更新应用镜像

当有新版本推送到 GitHub Packages 时：

```bash
# 更新镜像
kubectl set image deployment/bc-backend bc-backend=ghcr.io/bigchuang2024/bc-backend:latest -n bc-backend

# 等待滚动更新完成
kubectl rollout status deployment/bc-backend -n bc-backend
```

### 回滚部署

```bash
# 查看部署历史
kubectl rollout history deployment/bc-backend -n bc-backend

# 回滚到上一个版本
kubectl rollout undo deployment/bc-backend -n bc-backend
```

## 📊 监控和维护

### 资源监控

```bash
# 查看资源使用情况
kubectl top pods -n bc-backend
kubectl top nodes

# 查看事件
kubectl get events -n bc-backend --sort-by=.metadata.creationTimestamp
```

### 日志管理

```bash
# 查看所有 Pod 日志
kubectl logs -l app=bc-backend -n bc-backend

# 实时日志跟踪
kubectl logs -f deployment/bc-backend -n bc-backend
```

## 🗑️ 清理部署

```bash
# 删除所有资源
kubectl delete -f k8s/

# 删除命名空间
kubectl delete namespace bc-backend
```