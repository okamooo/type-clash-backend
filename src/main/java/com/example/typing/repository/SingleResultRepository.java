package com.example.typing.repository;

import com.example.typing.entity.SingleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SingleResultRepository extends JpaRepository<SingleResult, Integer> {
    Optional<SingleResult> findFirstByUserIdOrderByFinishedAtDesc(Integer userId);
    List<SingleResult> findAllByOrderByScoreDesc();
}