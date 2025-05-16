package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUserId(Long userId);
    Optional<UserToken> findByRefreshToken(String refreshToken);
    void deleteByUserId(Long userId);
}
