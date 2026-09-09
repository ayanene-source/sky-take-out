# 优选外卖点餐系统 

一个完整的外卖点餐系统，包含 Spring Boot 后端和微信小程序前端。

## 项目结构

```
├── sky-take-out/          # Java Spring Boot 后端
│   ├── sky-common/        # 公共模块（工具类、枚举、异常等）
│   ├── sky-pojo/          # 实体类 / DTO / VO
│   └── sky-server/        # 主服务（Controller、Service、Mapper）
├── mp-weixin/             # 微信小程序 - 用户端 (UniApp)
├── WxSkyTakeOut/          # 微信小程序（Skyline 渲染引擎）
└── nginx-1.20.2/         # Nginx 反向代理（不提交到仓库）
```

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.3 + Java 17
- **ORM**: MyBatis + PageHelper
- **数据库**: MySQL
- **缓存**: Redis
- **连接池**: Druid
- **认证**: JWT（双端 token）
- **文件存储**: 阿里云 OSS
- **支付**: 微信支付
- **API 文档**: Knife4j (Swagger)

### 前端
- **框架**: UniApp (Vue)
- **平台**: 微信小程序

## 快速开始

### 1. 配置环境变量

启动前需要设置以下环境变量（或修改 `application-dev.yml`）：

```bash
# 数据库
SKY_DB_HOST=localhost
SKY_DB_PORT=3306
SKY_DB_NAME=sky_take_out
SKY_DB_USERNAME=root
SKY_DB_PASSWORD=your_password

# Redis
SKY_REDIS_HOST=localhost
SKY_REDIS_PORT=6379
SKY_REDIS_DATABASE=0

# 阿里云 OSS
SKY_OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
SKY_OSS_ACCESS_KEY_ID=your_access_key_id
SKY_OSS_ACCESS_KEY_SECRET=your_access_key_secret
SKY_OSS_BUCKET_NAME=your_bucket_name

# 微信
SKY_WECHAT_APPID=your_appid
SKY_WECHAT_SECRET=your_appsecret

# JWT（生产环境务必修改默认值）
SKY_JWT_ADMIN_SECRET=your_admin_secret
SKY_JWT_USER_SECRET=your_user_secret
```

### 2. 启动后端

```bash
cd sky-take-out
mvn clean install
cd sky-server
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8080`

### 3. 启动 Nginx（可选）

```bash
cd nginx-1.20.2
nginx.exe
```

### 4. 微信小程序

使用微信开发者工具导入 `mp-weixin/` 或 `WxSkyTakeOut/` 目录。

## 架构说明

```
微信小程序(用户端) ──┐
                      ├──> Nginx (80) ──> Spring Boot (8080) ──> MySQL + Redis
微信小程序(管理端) ──┘                          │
                                    阿里云 OSS / 微信支付
```

## 许可证

仅供学习参考。
