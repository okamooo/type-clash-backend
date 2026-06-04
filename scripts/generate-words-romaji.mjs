/**
 * words テーブル用: kana_reading → romaji_target を生成する。
 *
 * ルール（日本向け・かな1文字ずつ固定）:
 * - スペース除去して連結
 * - ひらがな「は」→ 常に romaji "ha"（助詞・語頭どちらも。例: 今日は→kyouha…、はじめて→hajimete）
 * - ひらがな「わ」→ 常に romaji "wa"（例: 終わる→owaru、わらえ→warae）
 * - それ以外のかな塊 → wanakana（shi, chuu 等）
 */
import { toRomaji } from "wanakana";

const WORDS = [
  {
    display: "友達になるのにだって資格なんていらない",
    kana: "ともだちになるのにだってしかくなんていらない",
  },
  {
    display: "もうこれで終わってもいい だからありったけを",
    kana: "もうこれでおわってもいい だからありったけを",
  },
  {
    display: "クセになってんだ 音殺して動くの",
    kana: "くせになってんだ おところしてうごくの",
  },
  {
    display: "生殺与奪の権を他人に握らせるな",
    kana: "せいさつよだつのけんをたにんににぎらせるな",
  },
  { display: "判断が遅い", kana: "はんだんがおそい" },
  { display: "全集中", kana: "ぜんしゅうちゅう" },
  {
    display: "俺は長男だから我慢できたけど次男だったら我慢できなかった",
    kana: "おれはちょうなんだからがまんできたけどじなんだったらがまんできなかった",
  },
  { display: "お前も鬼にならないか", kana: "おまえもおににならないか" },
  { display: "海賊王に俺はなる", kana: "かいぞくおうにおれはなる" },
  { display: "命がもったいない", kana: "いのちがもったいない" },
  {
    display: "君のような勘のいいガキは嫌いだよ",
    kana: "きみのようなかんのいいがきはきらいだよ",
  },
  {
    display: "撃っていいのは打たれる覚悟のある奴だけだ",
    kana: "うっていいのはうたれるかくごのあるやつだけだ",
  },
  { display: "魔観光殺法", kana: "まかんこうさっぽう" },
  { display: "きたねえ花火だ", kana: "きたねえはなびだ" },
  { display: "駆逐してやる", kana: "くちくしてやる" },
  { display: "心臓を捧げよ", kana: "しんぞうをささげよ" },
  {
    display: "何の成果も得られませんでした",
    kana: "なんのせいかもえられませんでした",
  },
  {
    display: "これは父さんが始めた物語だろ",
    kana: "これはとうさんがはじめたものがたりだろ",
  },
  {
    display: "おまえは今まで食ったパンの枚数をおぼえているのか",
    kana: "おまえはいままでくったぱんのまいすうをおぼえているのか",
  },
  {
    display: "無駄無駄無駄無駄無駄無駄無駄無駄無駄無駄無駄無駄",
    kana: "むだむだむだむだむだむだむだむだむだむだむだむだ",
  },
  { display: "だが断る", kana: "だがことわる" },
  {
    display: "あきらめたらそこで試合終了ですよ",
    kana: "あきらめたらそこでしあいしゅうりょうですよ",
  },
  {
    display: "逃げちゃダメだ逃げちゃダメだ逃げちゃダメだ",
    kana: "にげちゃだめだ にげちゃだめだ にげちゃだめだ",
  },
  { display: "笑えばいいと思うよ", kana: "わらえばいいとおもうよ" },
  { display: "まだだ まだ終わらんよ", kana: "まだだ まだおわらんよ" },
  {
    display: "二度もぶった 親父にもぶたれたことないのに",
    kana: "にどもぶった おやじにもぶたれたことないのに",
  },
  {
    display: "認めたくないものだな 自分自身の若さゆえの過ちというものを",
    kana: "みとめたくないものだな じぶんじしんのわかさゆえのあやまちというものを",
  },
  {
    display: "アムロ 行きまーす",
    kana: "あむろ いきまーす",
    romaji: "amuroikimaasu",
  },
  { display: "計画通り", kana: "けいかくどおり" },
  { display: "新世界の神となる", kana: "しんせかいのかみとなる" },
  { display: "城之内死す", kana: "じょうのうちしす" },
  {
    display: "雑魚の罪は強さを知らんこと",
    kana: "ざこのつみはつよさをしらんこと",
  },
  {
    display: "最高速度でブチ抜いたる",
    kana: "さいこうそくどでぶちぬいたる",
  },
  {
    display: "三歩後ろを歩かれへん女は背中刺されて死んだらええ",
    kana: "さんぽうしろをあるかれへんおんなはせなかさされてしんだらええ",
  },
  { display: "ごめんちゃい", kana: "ごめんちゃい" },
  { display: "取柄のお顔もグズグズ", kana: "とりえのおかおもぐずぐず" },
  {
    display: "アッチ側に立つんは 俺や",
    kana: "あっちがわにたつんは おれや",
    romaji: "acchigawanitatsunhaoreya",
  },
  {
    display: "非道いなぁ 人の心とかないんか",
    kana: "ひどいなぁ ひとのこころとかないんか",
  },
  { display: "禪院家当主は俺や", kana: "ぜんいんけとうしゅはおれや" },
  {
    display: "俺は君を殺しに来てんねんで",
    kana: "おれはきみをころしにきてんねんで",
  },
  {
    display: "僕も来たで こっち側",
    kana: "ぼくもきたで こっちがわ",
    romaji: "bokumokitadekocchigawa",
  },
  {
    display: "やるやん 腐っても次代当主やね",
    kana: "やるやん くさってもじだいとうしゅやね",
  },
  { display: "がんばり賞ってとこやね", kana: "がんばりしょうってとこやね" },
  {
    display: "弟よりデキの悪い兄なんか居る意味ないやろ 首括って死んだらええねん",
    kana: "おとうとよりできのわるいあになんかおるいみないやろ くびくくってしんだらええねん",
  },
  {
    display: "顔がアカンわ 甚爾君と逆やったらよかったのにな",
    kana: "かおがあかんわ とうじくんとぎゃくやったらよかったのにな",
  },
  {
    display: "とりあえず足でも折っといたろかな",
    kana: "とりあえずあしでもおっといたろかな",
  },
  { display: "オマエは 甚爾君やない", kana: "おまえは とうじくんやない" },
  { display: "やっぱオマエは偽物や", kana: "やっぱおまえはにせものや" },
  { display: "死んでもらお思て", kana: "しんでもらおおもて" },
  { display: "二人まとめて殺したる", kana: "ふたりまとめてころしたる" },
];

/** wanakana の撥音区切り（'）や長音（-）はタイピング用に除去 */
function normalizeRomaji(romaji) {
  return romaji.replace(/['-]/g, "");
}

function isKanaChar(char) {
  return /[\u3040-\u309F]/.test(char) || char === "ー";
}

/** かな「は」→ ha、「わ」→ wa を DB 正として明示し、残りを toRomaji */
export function kanaToRomajiTarget(kanaReading) {
  const compact = kanaReading.replace(/[\s　]+/g, "");
  const parts = [];
  let buffer = "";

  const flushKana = () => {
    if (buffer.length > 0) {
      parts.push({ kind: "kana", value: buffer });
      buffer = "";
    }
  };

  for (let ki = 0; ki < compact.length; ki++) {
    const char = compact[ki];

    if (char === "は") {
      flushKana();
      parts.push({ kind: "ha" });
      continue;
    }

    if (char === "わ") {
      flushKana();
      parts.push({ kind: "wa" });
      continue;
    }

    if (isKanaChar(char)) {
      buffer += char;
      continue;
    }
  }

  flushKana();

  const joined = parts
    .map((part) => {
      if (part.kind === "ha") return "ha";
      if (part.kind === "wa") return "wa";
      return toRomaji(part.value);
    })
    .join("");
  return normalizeRomaji(joined);
}

function escapeSql(value) {
  return value.replace(/'/g, "''");
}

const lines = WORDS.map((word) => {
  const romaji = word.romaji ?? kanaToRomajiTarget(word.kana);
  return `('${escapeSql(word.display)}', '${escapeSql(word.kana)}', '${escapeSql(romaji)}')`;
});

console.log("INSERT INTO words (display_text, kana_reading, romaji_target) VALUES");
console.log(lines.join(",\n") + ";");
