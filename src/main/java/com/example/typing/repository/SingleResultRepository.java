package com.example.typing.repository;

import com.example.typing.entity.SingleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SingleResultRepository extends JpaRepository<SingleResult, Long> {
        Optional<SingleResult> findFirstByUserIdOrderByFinishedAtDesc(Long userId);

        List<SingleResult> findAllByOrderByScoreDesc();

        /**
         * ランキング取得（上位100件）
         * - ベストスコア時のレコードをユーザーごとに1件に絞り込み
         *   （同スコアの場合は正答率降順 → 最初のプレイ日時昇順で選択）
         * - RANK()でスコア降順 → 正答率降順で順位付け（同率同順位・順位スキップあり）
         * - 100位以内を返す（100位タイは全員含む）
         * - 同順位内はプレイ日時昇順で表示
         * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
         */
        @Query(value = """
                SELECT ranked.rank,
                       ranked.user_id,
                       ranked.name,
                       ranked.best_score,
                       ranked.accuracy_rate
                FROM (
                    SELECT RANK() OVER (
                               ORDER BY best.score         DESC,
                                        best.accuracy_rate DESC
                        )              AS rank,
                        best.user_id,
                        u.name,
                        best.score     AS best_score,
                        best.accuracy_rate,
                        best.finished_at
                FROM (
                    SELECT sr.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY sr.user_id
                               ORDER BY sr.score         DESC,
                                        sr.accuracy_rate DESC,
                                        sr.finished_at   ASC
                           ) AS row_num
                    FROM single_results sr
                ) best
                JOIN users u ON u.id = best.user_id
                WHERE best.row_num = 1
                  AND u.deleted_at IS NULL
                ) ranked
                WHERE ranked.rank <= 100
                ORDER BY ranked.rank        ASC,
                         ranked.finished_at ASC
                """, nativeQuery = true)
        List<Object[]> findRankings();

        /**
         * 総ユーザー数取得
         * - シングルゲームのスコアが1件以上あるユーザーが対象
         * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
         */
        @Query(value = """
                SELECT COUNT(DISTINCT sr.user_id)
                FROM single_results sr
                JOIN users u ON u.id = sr.user_id
                WHERE u.deleted_at IS NULL
                """, nativeQuery = true)
        int countTotalUsers();

        /**
         * 全ユーザーのベストスコアの平均を取得
         * - ユーザーごとのベストスコアを算出し、その平均を返す
         * - データなしの場合は0.0を返す
         * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
         */
        @Query(value = """
                SELECT COALESCE(AVG(best_score), 0.0)
                FROM (
                    SELECT MAX(sr.score) AS best_score
                    FROM single_results sr
                    JOIN users u ON u.id = sr.user_id
                    WHERE u.deleted_at IS NULL
                    GROUP BY sr.user_id
                ) user_bests
                """, nativeQuery = true)
        double findAverageBestScore();
}
