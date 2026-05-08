package com.example.typing.service;

import com.example.typing.entity.Words;
import com.example.typing.dto.response.SingleRankingResponse;
import com.example.typing.entity.SingleResult;
import com.example.typing.repository.WordRepository;
import com.example.typing.repository.SingleResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * 全ての単語を取得（フロント側でランダム表示・リスト保持するため）
     */
    public List<Words> getAllWords() {
        return wordRepository.findAll();
    }

    /**
     * DB 保存
     */
    @Transactional
    public int calculateAndSaveScore(Long userId, int rawScore, int correctCount, int totalInput) {
        if (totalInput <= 0) {
            throw new IllegalArgumentException("Total input must be greater than zero");
        }

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
    public SingleResult getLatestResult(Long userId) {
        return singleResultRepository.findFirstByUserIdOrderByFinishedAtDesc(userId).orElse(null);
    }

    /**
     * ランキングを取得
     * - ユーザーごとのベストスコアで集計し、順位を付与して返す
     * - データが存在しない場合は空リストを返す
     */
    public List<SingleRankingResponse> getRankings() {
        List<Object[]> rows = singleResultRepository.findRankings();

        List<SingleRankingResponse> rankings = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            rankings.add(new SingleRankingResponse(
                    i + 1, // 順位
                    (String) row[1], // ユーザー名
                    ((Number) row[2]).intValue(), // スコア
                    ((Number) row[3]).intValue() // 正答率
            ));
        }
        return rankings;
    }
}
