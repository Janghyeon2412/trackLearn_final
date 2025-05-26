package com.multi.tracklearn.service;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.UserStatus;
import com.multi.tracklearn.dto.ResetPasswordChangeDTO;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.repository.CategoryRepository;
import com.multi.tracklearn.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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

        if (!request.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,16}$")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자여야 합니다.");
        }

        Category category = (request.getCategoryId() != null)
                ? categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"))
                : categoryRepository.findByCode("ETC")
                .orElseThrow(() -> new IllegalStateException("기본 카테고리(ETC)가 DB에 없습니다."));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(request.getProfileImageUrl())
                .category(category)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    public User login(UserLoginDTO request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            log.warn("[로그인 실패] 존재하지 않는 이메일: {}", request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int failCount = Optional.ofNullable(user.getLoginFailCount()).orElse(0) + 1;
            user.setLoginFailCount(failCount);
            userRepository.save(user);

            log.warn("[로그인 실패] 이메일 : {}, 실패 횟수: {}", user.getEmail(), failCount);
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다. (" + failCount + "회 실패)");
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("탈퇴한 게정입니다.");
        }

        user.setLoginFailCount(0);
        userRepository.save(user);

        System.out.println("입력 비번: " + request.getPassword());
        System.out.println("DB 비번: " + user.getPassword());
        System.out.println("matches? " + passwordEncoder.matches(request.getPassword(), user.getPassword()));


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

        user.setStatus(UserStatus.DELETED);
        userRepository.delete(user);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .isPresent();
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

    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findOptionalByEmail(email);
    }


    public String findNicknameByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? user.getNickname() : "사용자";
    }


}