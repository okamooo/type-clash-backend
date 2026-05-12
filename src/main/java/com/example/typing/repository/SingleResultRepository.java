package com.example.typing.repository;

import com.example.typing.entity.SingleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SingleResultRepository extends JpaRepository<SingleResult, Long> {
        Optional<SingleResult> findFirstByUserIdOrderByFinishedAtDesc(Long userId);

        List<SingleResult> findAllByOrderByScoreDesc();

        /**
         * ランキング取得結果のマッピング用インターフェース
         */
        interface SingleRankingRow {
                Integer getRank(); // 順位
                Long getUserId(); // ユーザーID
                String getName(); // ユーザー名
                Integer getBestScore(); // ベストスコア
                Integer getAccuracyRate(); // 正答率（%）
                Integer getTotalUsers(); // 総ユーザー数
                Double getAverageBestScore(); // 平均ベストスコア
        }

        /**
         * ランキング・統計情報を一括取得
         * - ベストスコア時のレコードをユーザーごとに1件に絞り込み
         * （同スコアの場合は正答率降順 → プレイ日時降順で選択）
         * - RANK()でスコア降順 → 正答率降順で順位付け（同率同順位・順位スキップあり）
         * - 100位以内を返す
         * - 同順位内はプレイ日時降順で表示
         * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
         * - データなしの場合は空リストを返す
         */
        @Query(value = """
                WITH user_best_results AS (
                    SELECT
                        sr.*,
                        ROW_NUMBER() OVER (
                        PARTITION BY sr.user_id
                        ORDER BY
                                sr.score         DESC,
                                sr.accuracy_rate DESC,
                                sr.finished_at   DESC
                        ) AS row_num
                    FROM single_results sr
                    JOIN users u
                        ON u.id = sr.user_id
                    WHERE u.deleted_at IS NULL
                ),
                ranking_rows AS (
                    SELECT
                        RANK() OVER (
                        ORDER BY
                                ubr.score         DESC,
                                ubr.accuracy_rate DESC
                        ) AS rank,
                        ubr.user_id,
                        u.name,
                        ubr.score AS best_score,
                        ubr.accuracy_rate,
                        ubr.finished_at
                    FROM user_best_results ubr
                    JOIN users u
                        ON u.id = ubr.user_id
                    WHERE ubr.row_num = 1
                ),
                stats AS (
                    SELECT
                        COUNT(*)                  AS total_users,
                        COALESCE(AVG(score), 0.0) AS average_best_score
                    FROM user_best_results
                    WHERE row_num = 1
                )
                SELECT
                    rr.rank,
                    rr.user_id,
                    rr.name,
                    rr.best_score,
                    rr.accuracy_rate,
                    stats.total_users,
                    stats.average_best_score
                FROM ranking_rows rr
                CROSS JOIN stats
                WHERE rr.rank <= 100
                ORDER BY
                    rr.rank        ASC,
                    rr.finished_at DESC
                """, nativeQuery = true)
        List<SingleRankingRow> findRankingsWithStats();

        /**
         * 履歴取得結果のマッピング用インターフェース
         */
        interface SingleHistoryRow {
            LocalDateTime  getFinishedAt();   // プレイ日時
            Integer getScore();        // スコア
            Integer getAccuracyRate(); // 正答率（%）
            Integer getBestScore();    // 最高スコア
            Double  getAverageScore(); // 平均スコア
            Integer getPlayCount();    // プレイ回数
        }

        /**
         * 指定ユーザーの履歴・統計情報を一括取得
         * - 履歴は finished_at 降順（新しい順）で返す
         * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
         * - データなしの場合は空リストを返す
         */
        @Query(value = """
                SELECT
                    sr.finished_at,
                    sr.score,
                    sr.accuracy_rate,
                    MAX(sr.score)  OVER () AS best_score,
                    AVG(sr.score)  OVER () AS average_score,
                    COUNT(*)       OVER () AS play_count
                FROM single_results sr
                JOIN users u
                    ON u.id = sr.user_id
                WHERE sr.user_id   = :userId
                    AND u.deleted_at IS NULL
                ORDER BY sr.finished_at DESC
                """, nativeQuery = true)
        List<SingleHistoryRow> findHistoryByUserId(@Param("userId") Long userId);
}
