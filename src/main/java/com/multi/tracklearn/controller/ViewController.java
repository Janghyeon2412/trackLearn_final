package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.service.CategoryService;
import com.multi.tracklearn.service.UserService;
import com.multi.tracklearn.service.UserTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@SessionAttributes("userSignupDTO")
public class ViewController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenService userTokenService;

    public ViewController(CategoryService categoryService, UserService userService, JwtTokenProvider jwtTokenProvider, UserTokenService userTokenService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userTokenService = userTokenService;
    }

    @GetMapping("/signup/step1")
    public String showSignupStep1(Model model) {
        model.addAttribute("userSignupDTO", new UserSignupDTO());
        return "signup/step1";
    }

    @PostMapping("/signup/step1")
    public String handleSignupStep1(
            @Valid @ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO,
            @RequestParam("passwordCheck") String passwordCheck,
            BindingResult bindingResult,
            Model model) {

        // 1. 먼저 기본 유효성 검사 (@Email, @Size 등) 에러부터 체크
        if (bindingResult.hasErrors()) {
            return "signup/step1";
        }

        // 이메일 중복 체크
        if (userService.existsByEmail(userSignupDTO.getEmail())) {
            model.addAttribute("emailDuplicate", true);
            return "signup/step1";
        }

        // 2. 비밀번호 일치 여부 체크
        if (!userSignupDTO.getPassword().equals(passwordCheck)) {
            model.addAttribute("passwordMismatch", true);
            return "signup/step1";
        }



        // 3. 모든 검증 통과 시
        return "redirect:/signup/step2";
    }


    @GetMapping("/signup/step2")
    public String showSignupStep2(@ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "signup/step2";
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("userLoginDTO", new UserLoginDTO());  // DTO 넣어줘야 th:field가 인식함
        return "user/login";
    }


    @PostMapping("/signup/step2")
    public String handleSignupStep2(

            @ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO, BindingResult bindingResult, Model model, HttpServletResponse response) {

        Category category = categoryService.findById(userSignupDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        // 회원가입 처리
        User user = userService.signup(userSignupDTO);

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // refresh 토큰 저장
        userTokenService.saveToken(user, refreshToken, LocalDateTime.now().plusDays(7));

        // 토큰을 JS 에서 사용할 수 있도록 쿠키에 전달
        response.setHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; HttpOnly; Max-Age=604800");
        response.setHeader("Set-Cookie", "accessToken=" + accessToken + "; Path=/; Max-Age=7200");

        return "redirect:/main"; // 바로 메인 페이지로 리다이렉트
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute("userLoginDTO") UserLoginDTO loginDTO, BindingResult bindingResult, Model model) {
        try {
            userService.login(loginDTO);
            return "redirect:/main";
        } catch (IllegalArgumentException e) {
            model.addAttribute("loginError", e.getMessage());
            return "user/login";
        }
    }


}
