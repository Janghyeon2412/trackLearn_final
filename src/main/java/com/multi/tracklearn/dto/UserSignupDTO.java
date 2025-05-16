package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Category;
import com.multi.tracklearn.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupDTO {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식을 입력해주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, max = 16, message = "비밀번호는 8 ~ 16자 사이여야 합니다")
    private String password;

    private String nickname;
    private String profileImageUrl;
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
