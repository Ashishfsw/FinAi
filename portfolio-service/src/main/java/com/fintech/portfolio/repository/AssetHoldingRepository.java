package com.fintech.portfolio.repository;

import com.fintech.portfolio.entity.AssetHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetHoldingRepository extends JpaRepository<AssetHolding, Long> {
    List<AssetHolding> findByUserId(Long userId);
    List<AssetHolding> findByUserUsername(String username);
    Optional<AssetHolding> findByUserIdAndSymbol(Long userId, String symbol);
    void deleteByUserId(Long userId);
}
