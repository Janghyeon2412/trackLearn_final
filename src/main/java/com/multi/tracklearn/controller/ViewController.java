package com.multi.tracklearn.controller;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.dto.UserLoginDTO;
import com.multi.tracklearn.dto.UserSignupDTO;
import com.multi.tracklearn.service.CategoryService;
import com.multi.tracklearn.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes("userSignupDTO")
public class ViewController {

    private final CategoryService categoryService;
    private final UserService userService;

    public ViewController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
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

            @ModelAttribute("userSignupDTO") UserSignupDTO userSignupDTO, BindingResult bindingResult, Model model) {

        Category category = categoryService.findById(userSignupDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        System.out.println("step2에서 받은 이메일: " + userSignupDTO.getEmail());
        System.out.println("step2에서 받은 비번: " + userSignupDTO.getPassword());

        userService.signup(userSignupDTO);

        return "redirect:/login"; // 회원가입 완료 후 로그인 이동

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
