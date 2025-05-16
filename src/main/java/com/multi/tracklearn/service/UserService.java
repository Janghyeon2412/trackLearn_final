package com.multi.tracklearn.service;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.ResetPasswordChangeDTO;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.repository.CategoryRepository;
import com.multi.tracklearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenService userTokenService;

    // 인증 코드 저장
    private final Map<String, VerificationCodeData> verificationCodeStore = new ConcurrentHashMap<>();

    private static class VerificationCodeData {
        private final String code;
        private final long timestamp;


        private VerificationCodeData(String code, long timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }

        public String getCode() {
            return code;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    @Autowired
    public UserService(UserRepository userRepository, CategoryRepository categoryRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, UserTokenService userTokenService, UserTokenService userTokenService1) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userTokenService = userTokenService1;
    }

    public User signup(UserSignupDTO request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .category(category)
                .build();

        return userRepository.save(user);
    }

    public User login(UserLoginDTO request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Transactional
    public void deleteByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        userTokenService.deleteByUserId(user.getId());

        userRepository.delete(user);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByEmail(nickname);
    }

    public void sendResetPasswordEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // 6자리 임시 인증코드
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        long now = System.currentTimeMillis();
        verificationCodeStore.put(email, new VerificationCodeData(code, now));

        // 인증코드 출력
        System.out.println("[인증 코드] " + code + "(사용자 메일: " + email + ")");
    }


    public String resetPassword(ResetPasswordChangeDTO changeDTO) {
        User user = userRepository.findByEmail(changeDTO.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        VerificationCodeData verificationCodeData = verificationCodeStore.get(changeDTO.getEmail());
        if (verificationCodeData == null || !verificationCodeData.getCode().equals(changeDTO.getCode())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        long now = System.currentTimeMillis();
        if (now - verificationCodeData.getTimestamp() > 5 * 60 * 1000) { // 5분 초과
            throw new IllegalArgumentException("Expired verification code");
        }

        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(changeDTO.getNewPassword()));
        userRepository.save(user);

        // 인증코드 사용 후 제거
        verificationCodeStore.remove(changeDTO.getEmail());

        return jwtTokenProvider.generateToken(user);

    }
}