package com.multi.tracklearn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordChangeDTO {

    private String email;
    private String code;
    private String newPassword;

}
