package com.multi.tracklearn.controller;

import com.multi.tracklearn.auth.JwtTokenProvider;
import com.multi.tracklearn.auth.JwtUserAuthentication;
import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.dto.DiaryDetailDTO;
import com.multi.tracklearn.dto.DiaryEditDTO;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.service.*;
import com.multi.tracklearn.validation.ValidStep1;
import com.multi.tracklearn.validation.ValidStep2;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@SessionAttributes("userSignupDTO")
public class ViewController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserTokenService userTokenService;
    private final DiaryService diaryService;
    private final GoalService goalService;
    private final UserSettingService userSettingService;

    public ViewController(CategoryService categoryService, UserService userService, JwtTokenProvider jwtTokenProvider, UserTokenService userTokenService, DiaryService diaryService, GoalService goalService, UserSettingService userSettingService) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userTokenService = userTokenService;
        this.diaryService = diaryService;
        this.goalService = goalService;
        this.userSettingService = userSettingService;
    }

    @GetMapping("/signup/step1")
    public String showSignupStep1(Model model) {
        model.addAttribute("userSignupDTO", new UserSignupDTO());
        return "signup/step1";
    }

    @PostMapping("/signup/step1")
    public String handleSignupStep1(
            @Validated(ValidStep1.class) @ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO,
            BindingResult bindingResult,
            @RequestParam("passwordCheck") String passwordCheck,
            Model model) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(System.out::println);
            return "signup/step1";
        }


        if (userService.existsByEmail(userSignupDTO.getEmail())) {
            model.addAttribute("emailDuplicate", true);
            return "signup/step1";
        }

        if (!userSignupDTO.getPassword().equals(passwordCheck)) {
            model.addAttribute("passwordMismatch", true);
            return "signup/step1";
        }

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
            @Validated(ValidStep2.class) @ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "signup/step2";
        }

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
    public String handleLogin(
            @Valid @ModelAttribute("userLoginDTO") UserLoginDTO loginDTO,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return "user/login";
        }

        try {
            User user = userService.login(loginDTO);

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            userTokenService.saveToken(user, refreshToken, LocalDateTime.now().plusDays(7));

            response.setHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; HttpOnly; Max-Age=604800");
            response.setHeader("Set-Cookie", "accessToken=" + accessToken + "; Path=/; Max-Age=7200");

            return "redirect:/main";
        } catch (IllegalArgumentException e) {
            model.addAttribute("loginError", e.getMessage());
            return "user/login";
        }
    }



    @GetMapping("/main")
    public String showMainPage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("nickname", user.getNickname());
        return "dashboard/main";
    }



    @GetMapping("/test")
    public String testPage() {
        return "test";
    }

    @GetMapping("/diary/write")
    public String showDiaryWritePage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("todayGoals", goalService.getTodayGoals(user.getEmail()));
        model.addAttribute("diaryEdit", new DiaryEditDTO());
        model.addAttribute("userSetting", userSettingService.getSetting(user.getId()));

        return "diary/write";
    }





    @GetMapping("/diary/history")
    public String showDiaryHistoryPage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("nickname", user.getNickname());
        return "diary/diary-list";
    }


    @GetMapping("/diary/edit/{goalLogId}")
    public String editDiaryForm(@PathVariable Long goalLogId,
                                @AuthenticationPrincipal User user,
                                Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        DiaryEditDTO dto = diaryService.prepareEditForm(goalLogId, user.getEmail());

        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("diaryEdit", dto);

        return "diary/edit";
    }


    @GetMapping("/diary/detail/{diaryId}")
    public String showDiaryDetail(@PathVariable Long diaryId,
                                  @AuthenticationPrincipal User user,
                                  Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        DiaryDetailDTO dto = diaryService.getDiaryDetail(diaryId, user.getEmail());
        model.addAttribute("diary", dto);

        return "diary/detail";
    }


    @Controller
    public class StatsPageController {

        @GetMapping("/stats")
        public String showStatsPage(@AuthenticationPrincipal User user, Model model) {
            if (user == null) return "redirect:/login";
            model.addAttribute("nickname", user.getNickname());
            return "stats";
        }

    }

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) return "redirect:/login";
        model.addAttribute("nickname", user.getNickname());
        return "mypage";
    }

    @GetMapping("/settings")
    public String showSettingsPage(@AuthenticationPrincipal User user, Model model) {
        if (user == null) return "redirect:/login";
        model.addAttribute("nickname", user.getNickname());
        return "settings";
    }


}
