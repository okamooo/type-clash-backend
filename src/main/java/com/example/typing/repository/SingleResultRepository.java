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
     * - ユーザーごとのベストスコアで集計
     * - 並び順: スコア降順 → 正答率降順 → 最初のプレイ日時昇順（早い人が上位）
     * - 退会済みユーザー（deleted_at IS NOT NULL）は除外
     */
    @Query(value = """
            SELECT sr.user_id, u.name, MAX(sr.score), CAST(AVG(sr.accuracy_rate) AS integer)
            FROM single_results sr
            JOIN users u ON u.id = sr.user_id
            WHERE u.deleted_at IS NULL
            GROUP BY sr.user_id, u.name
            ORDER BY MAX(sr.score) DESC,
                     AVG(sr.accuracy_rate) DESC,
                     MIN(sr.finished_at) ASC
            LIMIT 100
            """, nativeQuery = true)
    List<Object[]> findRankings();
}
