# API エンドポイント一覧

## WelcomeController

| メソッド | パス | 説明 |
|--------|------|------|
| GET | `/` | トップページ |

## OwnerController

| メソッド | パス | 説明 |
|--------|------|------|
| GET | `/owners/new` | オーナー作成フォーム表示 |
| POST | `/owners/new` | オーナー作成処理 |
| GET | `/owners/find` | オーナー検索フォーム表示 |
| GET | `/owners` | オーナー一覧・検索結果表示 |
| GET | `/owners/{ownerId}` | オーナー詳細表示 |
| GET | `/owners/{ownerId}/edit` | オーナー編集フォーム表示 |
| POST | `/owners/{ownerId}/edit` | オーナー更新処理 |

## PetController

| メソッド | パス | 説明 |
|--------|------|------|
| GET | `/owners/{ownerId}/pets/new` | ペット作成フォーム表示 |
| POST | `/owners/{ownerId}/pets/new` | ペット作成処理 |
| GET | `/owners/{ownerId}/pets/{petId}/edit` | ペット編集フォーム表示 |
| POST | `/owners/{ownerId}/pets/{petId}/edit` | ペット更新処理 |

## VetController

| メソッド | パス | 説明 |
|--------|------|------|
| GET | `/vets.html` | 獣医師一覧画面 (HTML) |
| GET | `/vets` | 獣医師一覧 (JSON API) |

## CrashController

| メソッド | パス | 説明 |
|--------|------|------|
| GET | `/oups` | 例外デモ用エンドポイント |
