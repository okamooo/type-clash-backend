-- users サンプルデータ
INSERT INTO users (
    name,
    email,
    password,
    icon_image,
    background_image
) VALUES
('user1', 'user1@example.com', 'password', NULL, 0),
('user2', 'user2@example.com', 'password', NULL, 1);

-- シングルモード用サンプル単語
INSERT INTO words (display_text, kana_reading, romaji_target) VALUES 
('こんにちは', 'こんにちは', 'konnichiwa'),
('ありがとう', 'ありがとう', 'arigatou'),
('プログラミング', 'ぷろぐらみんぐ', 'puroguraminngu'),
('タイピング', 'たいぴんぐ', 'taipinngu'),
('ジャバスクリプト', 'じゃばすくりぷと', 'jabasukuriputo'),
('スプリングブート', 'すぷりんぐぶーと', 'supurinngubu-to'),
('データベース', 'でーたべーす', 'de-tabe-su');

-- 対戦モード用マジックワード（少し長め・難しめ）
INSERT INTO magic_words (magic_text, magic_reading, magic_target) VALUES 
('ファイアボール', 'ふぁいあぼーる', 'faiabo-ru'),
('サンダーストーム', 'さんだーすとーむ', 'sannda-suto-mu'),
('アイスブリザード', 'あいすぶりざーど', 'aisuburiza-do'),
('ダークボルテックス', 'だーくぼるてっくす', 'da-kuborutekkusu'),
('ホーリーライトニング', 'ほーりーらいとにんぐ', 'ho-ri-raitoninngu');

-- -- シングルモードランキング確認用 プレイ結果データ
-- -- user1: 2回プレイ（ベストスコア980が使われる）
-- -- user2: 1回プレイ
-- INSERT INTO single_results (user_id, score, accuracy_rate, finished_at) VALUES
-- (1, 980, 95, NOW()),
-- (1, 800, 80, NOW()),
-- (2, 850, 88, NOW());
