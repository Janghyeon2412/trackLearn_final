package com.multi.tracklearn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiarySaveDTO {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Size(min = 10, message = "내용은 최소 10자 이상이어야 합니다.")
    private String content;

    @Size(max = 30, message = "회고는 30자 이내로 작성해주세요.")
    private String summary;

    @Min(value = 0, message = "만족도는 0 이상이어야 합니다.")
    @Max(value = 5, message = "만족도는 5 이하여야 합니다.")
    private int satisfaction;

    @Min(value = 0, message = "공부 시간은 0분 이상이어야 합니다.")
    private int studyTime;

    private List<Long> completedGoalIds;

    private List<String> retrospectives;
}
