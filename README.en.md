# Premium Food Ordering System

[简体中文](README.md) | [日本語](README.ja.md)

A complete food-ordering system with a Spring Boot backend and WeChat Mini Program clients.

## Project structure

```text
├── sky-take-out/          # Java Spring Boot backend
│   ├── sky-common/        # Shared module: utilities, enums, exceptions, etc.
│   ├── sky-pojo/          # Entities, DTOs, and VOs
│   └── sky-server/        # Main service: controllers, services, and mappers
├── mp-weixin/             # WeChat Mini Program, customer client (UniApp)
├── WxSkyTakeOut/          # WeChat Mini Program using the Skyline renderer
└── nginx-1.20.2/          # Nginx reverse proxy (not committed to this repository)
```

## Technology stack

### Backend

- Framework: Spring Boot 2.7.3 and Java 17
- ORM: MyBatis and PageHelper
- Database: MySQL
- Cache: Redis
- Connection pool: Druid
- Authentication: JWT with separate tokens for admin and customer clients
- File storage: Alibaba Cloud OSS
- Payments: WeChat Pay
- API documentation: Knife4j (Swagger)

### Frontend

- Framework: UniApp (Vue)
- Platform: WeChat Mini Program

## Quick start

### 1. Configure environment variables

Set the following environment variables before starting, or update `sky-take-out/sky-server/src/main/resources/application-dev.yml`:

```bash
# Database
SKY_DB_HOST=localhost
SKY_DB_PORT=3306
SKY_DB_NAME=sky_take_out
SKY_DB_USERNAME=root
SKY_DB_PASSWORD=your_password

# Redis
SKY_REDIS_HOST=localhost
SKY_REDIS_PORT=6379
SKY_REDIS_DATABASE=0

# Alibaba Cloud OSS
SKY_OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
SKY_OSS_ACCESS_KEY_ID=your_access_key_id
SKY_OSS_ACCESS_KEY_SECRET=your_access_key_secret
SKY_OSS_BUCKET_NAME=your_bucket_name

# WeChat
SKY_WECHAT_APPID=your_appid
SKY_WECHAT_SECRET=your_appsecret

# JWT: change the default values in production
SKY_JWT_ADMIN_SECRET=your_admin_secret
SKY_JWT_USER_SECRET=your_user_secret
```

### 2. Start the backend

```bash
cd sky-take-out
mvn clean install
cd sky-server
mvn spring-boot:run
```

The service runs on `http://localhost:8080` by default.

### 3. Start Nginx (optional)

```bash
cd nginx-1.20.2
nginx.exe
```

### 4. WeChat Mini Program

Import either `mp-weixin/` or `WxSkyTakeOut/` with WeChat Developer Tools.

## Architecture

```text
WeChat Mini Program (customer) ──┐
                                  ├──> Nginx (80) ──> Spring Boot (8080) ──> MySQL + Redis
WeChat Mini Program (admin) ─────┘                          │
                                              Alibaba Cloud OSS / WeChat Pay
```

## License

For learning and reference only.
