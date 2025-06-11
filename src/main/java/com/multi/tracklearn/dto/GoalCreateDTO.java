package com.multi.tracklearn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoalCreateDTO {

    private Long goalId;

    @NotBlank(message = "목표 제목을 입력해주세요.")
    private String title;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;

    @NotBlank(message = "반복 유형을 선택해주세요.")
    @Pattern(regexp = "DAILY|WEEKLY|CUSTOM", message = "유효한 반복 유형이어야 합니다.")
    private String repeatType;

    private String repeatValue; // CUSTOM
    private String createdValue;

    private String goalDetail;
    private String goalReason;
    private String learningStyle;

}
