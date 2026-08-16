# Spring PetClinic 概要

Spring Boot 公式サンプルをベースにした、動物病院（Pet Clinic）向け Web アプリケーションです。飼い主・ペット・診察・獣医師の登録と参照を、ブラウザ UI から操作できます。このリポジトリでは、商品登録 REST API や GitLab CI / Cursor Automation などの拡張も入っています。

起動後は [http://localhost:8080](http://localhost:8080) で利用します。

## 技術スタック


| 項目       | 内容                                          |
| -------- | ------------------------------------------- |
| 言語 / JDK | Java 17                                     |
| フレームワーク  | Spring Boot 3.5.0                           |
| Web      | Spring MVC + Thymeleaf                      |
| 永続化      | Spring Data JPA（Hibernate DDL は無効、SQL で初期化） |
| バリデーション  | Bean Validation (Jakarta)                   |
| キャッシュ    | JCache + Caffeine（獣医師一覧など）                  |
| ビルド      | Maven (`./mvnw`) / Gradle (`./gradlew`)     |
| デフォルト DB | インメモリ H2（起動時に schema / data を投入）            |
| 任意 DB    | MySQL 9.2 / PostgreSQL 17.5（プロファイル切替）       |
| 監視       | Spring Boot Actuator（エンドポイント全公開）            |




## ドメインモデル

```
Owner (飼い主) 1 ── N Pet (ペット) 1 ── N Visit (診察)
                      N ── 1 PetType (犬・猫など)

Vet (獣医師) N ── N Specialty (専門分野)

Product (商品)  ※ このリポジトリ独自の REST 登録対象
```

- `Owner` は氏名・住所・電話番号を持ち、配下の `Pet` をカスケード保存する。ここで変更もできるのか。
- `Pet` は名前・生年月日・種類を持ち、`Visit`（日付と説明）を蓄積する。
- `Vet` は専門分野の多対多関連を持つ。
- 基底クラスは `BaseEntity`（ID）→ `NamedEntity` / `Person`。



## 主な機能とエンドポイント



### ブラウザ UI（Thymeleaf）


| 機能          | メソッド / パス                                          | 担当                  |
| ----------- | -------------------------------------------------- | ------------------- |
| ホーム         | `GET /`                                            | `WelcomeController` |
| 飼い主検索       | `GET /owners/find`, `GET /owners?lastName=`        | `OwnerController`   |
| 飼い主登録・更新・詳細 | `/owners/new`, `/owners/{id}`, `/owners/{id}/edit` | `OwnerController`   |
| ペット登録・更新    | `/owners/{ownerId}/pets/new`, `/pets/{petId}/edit` | `PetController`     |
| 診察登録        | `/owners/{ownerId}/pets/{petId}/visits/new`        | `VisitController`   |
| 獣医師一覧（HTML） | `GET /vets.html`                                   | `VetController`     |
| 例外デモ        | `GET /oups`                                        | `CrashController`   |


検索・一覧は 5 件ページングです。飼い主検索は姓の前方一致で、1 件なら詳細へリダイレクトします。

### JSON / REST


| 機能    | メソッド / パス            | 備考                                 |
| ----- | -------------------- | ---------------------------------- |
| 獣医師一覧 | `GET /vets`          | XML/JSON 向けの `Vets` ラッパー           |
| 商品登録  | `POST /api/products` | 成功時 `201 Created`。バリデーション失敗は `400` |


商品登録のリクエスト例:

- `productName` / `category`: 1〜100 文字、必須
- `price`: 0〜1,000,000
- `stock`: 0〜10,000



## アーキテクチャ

層は薄く、Service 層はほぼありません。Controller が Repository を直接呼び、ドメインオブジェクトに業務ルールを寄せています。

```
ブラウザ / API クライアント
        │
   Controller（owner / vet / product / system）
        │
   Repository（Spring Data JPA）
        │
   Entity（JPA） ── schema.sql / data.sql
        │
   View（Thymeleaf） ※ HTML 画面のみ
```

- 国際化: `messages/messages*.properties`（en / ja / ko / es など）
- エラー画面は例外メッセージを返さない（情報漏洩防止）
- シーケンス図は `[system_sequence.md](system_sequence.md)` を参照



## データベースと起動

デフォルトは H2。H2 コンソールは `http://localhost:8080/h2-console`。

永続 DB を使う場合はプロファイルを切り替えます。

```bash
# ローカル（H2）
./mvnw spring-boot:run
# または
./gradlew bootRun

# MySQL / PostgreSQL
docker compose up mysql      # spring.profiles.active=mysql
docker compose up postgres   # spring.profiles.active=postgres
```

Kubernetes 向けマニフェストは `k8s/`（アプリは postgres プロファイル、DB は別 Deployment）。

## このリポジトリ固有の拡張

公式 PetClinic に対して、次が加わっています。

- **商品登録 API**（`product` パッケージ、`products` テーブル）
- **GitLab CI**（`.gitlab-ci.yml`）: Maven ビルド、SAST / Secret Detection / Dependency Scanning、SLSA provenance、Renovate
- **GitLab Duo** 向け MR レビュー指示（日本語・Spring 観点）
- **Cursor Automation**: CI 完了時に webhook で Cloud Agent を起動し、東京の天気を `weather.md` に追記



## ディレクトリ案内


| パス                                          | 役割                      |
| ------------------------------------------- | ----------------------- |
| `src/main/java/.../owner`                   | 飼い主・ペット・診察              |
| `src/main/java/.../vet`                     | 獣医師                     |
| `src/main/java/.../product`                 | 商品 REST API             |
| `src/main/java/.../system`                  | ホーム、キャッシュ、例外、Web 設定     |
| `src/main/java/.../model`                   | 共通エンティティ基底              |
| `src/main/resources/templates`              | Thymeleaf 画面            |
| `src/main/resources/db/{h2,mysql,postgres}` | スキーマと初期データ              |
| `src/test/java`                             | コントローラ / XSS / DB 統合テスト |
| `.gitlab-ci.yml` / `.gitlab/duo`            | CI と Duo フロー            |
| `k8s/`                                      | Kubernetes マニフェスト       |




## ライセンス

Apache License 2.0（Spring PetClinic サンプルに準拠）