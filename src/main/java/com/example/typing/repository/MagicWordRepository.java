package com.example.typing.repository;

import com.example.typing.entity.MagicWords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MagicWordRepository extends JpaRepository<MagicWords, Integer> {
    @Query(value = "SELECT * FROM magic_words ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<MagicWords> findRandomMagicWord();
}
