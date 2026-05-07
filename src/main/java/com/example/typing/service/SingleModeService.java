package com.example.typing.service;

import com.example.typing.entity.Words;
import com.example.typing.entity.SingleResult;
import com.example.typing.repository.WordRepository;
import com.example.typing.repository.SingleResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SingleModeService {

    private final WordRepository wordRepository;
    private final SingleResultRepository singleResultRepository;

    // コンストラクタ注入
    public SingleModeService(WordRepository wordRepository, SingleResultRepository singleResultRepository) {
        this.wordRepository = wordRepository;
        this.singleResultRepository = singleResultRepository;
    }

    /**
     * ゲーム用の単語を取得
     */
    public List<Words> getWordsForGame(int limit) {
        return wordRepository.findRandomWords(limit);
    }

    /**
     * DB 保存
     */
    @Transactional
    public int calculateAndSaveScore(Integer userId, int rawScore, int correctCount, int totalInput) {
        if (totalInput == 0)
            return 0;

        // 1. スコアと正答率（Accuracy）の計算
        double accuracy = (double) correctCount / totalInput;
        int verifiedScore = (int) Math.ceil(rawScore * accuracy);
        int accuracyPercentage = (int) (accuracy * 100); // %表記で保存する場合

        // 2. Entity の作成と値のセット（image_116c18.png に準拠）
        SingleResult result = new SingleResult();
        result.setUserId(userId);
        result.setScore(verifiedScore);
        result.setAccuracyRate(accuracyPercentage);
        result.setFinishedAt(LocalDateTime.now()); // 本来は終了時間をフロントから貰うのが理想ですが、一旦現在時刻

        // 3. DB 保存
        singleResultRepository.save(result);

        return verifiedScore;
        }

        /**
        * 最新のスコアを取得
        */
        public SingleResult getLatestResult(Integer userId) {
        return singleResultRepository.findFirstByUserIdOrderByFinishedAtDesc(userId).orElse(null);
        }

    /**
     * ランキングを取得
     */
    public List<SingleResult> getRankings() {
        return singleResultRepository.findAllByOrderByScoreDesc();
    }
}