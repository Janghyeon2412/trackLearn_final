package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.validation.ValidStep1;
import com.multi.tracklearn.validation.ValidStep2;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserSignupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "이메일을 입력해주세요", groups = ValidStep1.class)
    @Email(message = "올바른 이메일 형식을 입력해주세요", groups = ValidStep1.class)
    @Size(max = 100, message = "이메일은 최대 100자까지 가능합니다", groups = ValidStep1.class)

    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요", groups = ValidStep1.class)
    @Size(min = 8, max = 16, message = "비밀번호는 8 ~ 16자 사이여야 합니다", groups = ValidStep1.class)
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~`|\\\\]).{8,16}$",
            message = "비밀번호는 8~16자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.",
            groups = ValidStep1.class
    )
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.", groups = ValidStep2.class)
    @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.", groups = ValidStep2.class)
    private String nickname;

    private String profileImageUrl;

    @NotNull(message = "관심 카테고리를 선택해주세요.", groups = ValidStep2.class)
    private Long categoryId;

    public User toEntity(Category category, String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .category(category)
                .build();
    }
}
