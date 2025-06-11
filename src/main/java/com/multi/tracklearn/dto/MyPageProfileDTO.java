package com.multi.tracklearn.dto;

import lombok.Getter;

@Getter
public class MyPageProfileDTO {
    private String nickname;
    private String email;
    private String profileImageUrl;

    public MyPageProfileDTO(String nickname, String email, String profileImageUrl) {
        this.nickname = nickname;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }
}