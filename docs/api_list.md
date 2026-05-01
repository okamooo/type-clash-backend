# API一覧

| メソッド | URL | 説明 |
|---|---|---|
| POST | `/api/users/register` | 新規ユーザーを登録する。名前・メールアドレス・パスワード等を受け取りアカウントを作成する |
| POST | `/api/auth/login` | メールアドレスとパスワードでログインする。成功時にセッションまたはトークンを返す |
| POST | `/api/auth/logout` | ログイン中のユーザーをログアウトする。セッションまたはトークンを無効化する |
| GET | `/api/users/:userId` | 指定したユーザーIDのユーザー情報を取得する |
| PATCH | `/api/users/:userId` | 指定したユーザーの情報（名前・メールアドレス・パスワード・アイコン画像）を更新する |
| DELETE | `/api/users/:userId` | 指定したユーザーを論理削除する（deleted_atに削除日時をセットする） |
| GET | `/api/words` | シングルモードで出題するワードを1件取得する |
| GET | `/api/magic-words` | 対戦モードで出題するワードを1件取得する |
| POST | `/api/users/:userId/results` | 指定したユーザーのシングルモードのプレイ結果（スコア・正答率など）を登録する |
| GET | `/api/users/:userId/results/latest` | 指定したユーザーの直近のシングルモード結果を1件取得する |
| GET | `/api/rankings` | シングルモードのスコア順ランキングを取得する |
| POST | `/api/matches/queue/join` | 対戦待機キューに参加する。マッチング待ち状態になる |
| DELETE | `/api/matches/queue/leave` | 対戦待機をキャンセルしてキューから離脱する |
| POST | `/api/matches` | キューで成立したマッチを元に対戦を開始する。対戦IDを発行する |
| GET | `/api/matches/:matchId` | 指定した対戦IDの現在の対戦状態（進行中・終了など）を取得する |
| POST | `/api/matches/:matchId/results` | 指定した対戦の結果（スコア・正答率・勝者など）を登録する |
| GET | `/api/matches/:matchId/results` | 指定した対戦の結果を取得する |
