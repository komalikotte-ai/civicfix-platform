package com.civicfix.platform.auth.repository;

import com.civicfix.platform.auth.entity.RefreshToken;
import com.civicfix.platform.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalseAndExpiryDateAfter(User user, Instant now);

    List<RefreshToken> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.user = :user AND r.revoked = false")
    void revokeAllUserTokens(@Param("user") User user, @Param("now") Instant now);
}
