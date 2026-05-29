package com.example.typing.repository;

import com.example.typing.entity.BattleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleResultRepository extends JpaRepository<BattleResult, Long> {
    java.util.Optional<BattleResult> findTopByOrderByMatchIdDesc();
}
