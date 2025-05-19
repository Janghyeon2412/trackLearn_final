package com.multi.tracklearn.service;

import com.multi.tracklearn.domain.UserToken;
import com.multi.tracklearn.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.multi.tracklearn.domain.User;


import java.time.LocalDateTime;

@Service
public class UserTokenService {

    private final UserTokenRepository userTokenRepository;

    @Autowired
    public UserTokenService(UserTokenRepository userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    public void saveToken(User user, String refreshToken, LocalDateTime expiresAt) {
        UserToken token = userTokenRepository.findByUserId(user.getId())
                .orElse(new UserToken());

        token.setUser(user);
        token.setRefreshToken(refreshToken);
        token.setExpiresDate(expiresAt);
        token.setCreatedPerson("system");
        token.setModifiedPerson("system");

        userTokenRepository.save(token);
    }

    public UserToken getByRefreshToken(String refreshToken) {
        UserToken token = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        if (token.getExpiresDate().isBefore(LocalDateTime.now())) {
            userTokenRepository.delete(token); // 만료된 토큰 삭제
            throw new IllegalArgumentException("Refresh token expired");
        }

        return token;
    }

    public void deleteByUserId(Long userId) {
        userTokenRepository.deleteByUserId(userId);
    }

    public User getUserByRefreshToken(String refreshToken) {
        UserToken userToken = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (userToken.getExpiresDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return userToken.getUser();
    }


}