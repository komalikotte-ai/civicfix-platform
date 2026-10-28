package com.civicfix.platform.auth.repository;

import com.civicfix.platform.auth.entity.EmailVerificationToken;
import com.civicfix.platform.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
}
