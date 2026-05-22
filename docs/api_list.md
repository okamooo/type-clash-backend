### API一覧

### ■ ユーザー・認証

| メソッド | URL | 説明 |
| --- | --- | --- |
| POST | `/api/auth/login` | メールアドレスとパスワードでログインする。成功時にセッションまたはトークンを返す |
| POST | `/api/auth/logout` | ログイン中のユーザーをログアウトする。セッションまたはトークンを無効化する |
| POST | `/api/auth/otp/register` | ユーザー登録時にワンタイムパスワードをメールに送り、otp_tokensテーブルに一時保存する |
| POST | `/api/auth/otp/verify` | ワンタイムパスワードを検証する。成功時に `registerToken`（scope=REGISTER・10分有効）をhttpOnly Cookieにセットする |
| POST | `/api/auth/registerUser` | `registerToken` Cookie を検証し、otp_tokensのデータを使ってusersテーブルへ本登録する |
| GET | `/api/users/:userId` | 指定したユーザーIDのユーザー情報を取得する |
| PATCH | `/api/users/:userId` | 指定したユーザーの情報（名前・メールアドレス・パスワード・アイコン画像）を更新する |
| DELETE | `/api/users/:userId` | 指定したユーザーを論理削除する（deleted_atに削除日時をセットする） |

---

### ■ シングルモード

| メソッド | URL | 説明 |
| --- | --- | --- |
| GET | `/api/words` | シングルモードで出題するワードを全件取得する |
| POST | `/api/single-results` | シングルモードのプレイ結果（ユーザーID・スコア・正答率など）を登録する |
| GET | `/api/single-results/latest?userId=:userId` | 指定したユーザーの最近のシングルモード結果を1件取得する |
| GET | `/api/single-results/rankings` | シングルモードのスコア順ランキングを取得する |
| GET | `/api/single-results/history?userId=:userId` | 指定したユーザーのスコア履歴を取得する |

---

### ■ 対戦モード（結果）

| メソッド | URL | 説明 |
| --- | --- | --- |
| GET | `/api/magic-words` | 対戦モードで出題するワードを全件取得する |
| POST | `/api/battle-results` | 対戦の結果（対戦ID・スコア・正答率・勝者など）を登録する |
| GET | `/api/battle-results/:id` | 指定した対戦結果IDのデータを1件取得する |

---

### ■ マッチング

| メソッド | URL | 説明 |
| --- | --- | --- |
| POST | `/api/battles/queue/join` | 対戦待機キューに参加する。マッチング待ち状態になる |
| POST | `/api/battles/queue/leave` | 対戦待機をキャンセルしてキューから離脱する（ブラウザ離脱時の通知を含む） |
| DELETE | `/api/battles/queue/leave` | 【非推奨】対戦待機をキャンセルしてキューから離脱する（WebSocketまたはREST API） |
| POST | `/api/battles` | キューで成立した対戦を開始する。対戦IDを発行する |
| GET | `/api/battles/:battleId` | 指定した対戦IDの現在の対戦状態（進行中・終了など）を取得する |
