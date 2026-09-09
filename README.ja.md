# 優選フード注文システム

[简体中文](README.md) | [English](README.en.md)

Spring Boot バックエンドと WeChat ミニプログラムのクライアントで構成される、総合的なフード注文システムです。

## プロジェクト構成

```text
├── sky-take-out/          # Java Spring Boot バックエンド
│   ├── sky-common/        # 共通モジュール：ユーティリティ、列挙型、例外など
│   ├── sky-pojo/          # エンティティ、DTO、VO
│   └── sky-server/        # メインサービス：コントローラー、サービス、Mapper
├── mp-weixin/             # WeChat ミニプログラム：利用者向け（UniApp）
├── WxSkyTakeOut/          # Skyline レンダラーを使用する WeChat ミニプログラム
└── nginx-1.20.2/          # Nginx リバースプロキシ（このリポジトリには未コミット）
```

## 技術スタック

### バックエンド

- フレームワーク：Spring Boot 2.7.3、Java 17
- ORM：MyBatis、PageHelper
- データベース：MySQL
- キャッシュ：Redis
- コネクションプール：Druid
- 認証：管理者・利用者向けに分離した JWT トークン
- ファイルストレージ：Alibaba Cloud OSS
- 決済：WeChat Pay
- API ドキュメント：Knife4j（Swagger）

### フロントエンド

- フレームワーク：UniApp（Vue）
- プラットフォーム：WeChat ミニプログラム

## クイックスタート

### 1. 環境変数を設定する

起動前に以下の環境変数を設定するか、`sky-take-out/sky-server/src/main/resources/application-dev.yml` を編集します。

```bash
# データベース
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

# JWT：本番環境では既定値を必ず変更してください
SKY_JWT_ADMIN_SECRET=your_admin_secret
SKY_JWT_USER_SECRET=your_user_secret
```

### 2. バックエンドを起動する

```bash
cd sky-take-out
mvn clean install
cd sky-server
mvn spring-boot:run
```

既定では `http://localhost:8080` で起動します。

### 3. Nginx を起動する（任意）

```bash
cd nginx-1.20.2
nginx.exe
```

### 4. WeChat ミニプログラム

WeChat Developer Tools で `mp-weixin/` または `WxSkyTakeOut/` をインポートします。

## アーキテクチャ

```text
WeChat ミニプログラム（利用者） ──┐
                                  ├──> Nginx (80) ──> Spring Boot (8080) ──> MySQL + Redis
WeChat ミニプログラム（管理者） ──┘                          │
                                             Alibaba Cloud OSS / WeChat Pay
```

## ライセンス

学習および参照目的に限ります。
