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
