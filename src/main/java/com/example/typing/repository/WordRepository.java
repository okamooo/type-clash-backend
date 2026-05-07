package com.example.typing.repository;

import com.example.typing.entity.Words;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Words, Long> {

    /**
     * PostgreSQLのRANDOM()関数を使用して、指定した件数分ランダムに単語を取得する。
     * 画像 image_11e0d6.png のテーブル名 'words' に合わせてクエリを構成しています。
     */
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Words> findRandomWords(@Param("limit") int limit);
}