package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.Goal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoalListDTO {

    private Long id;
    private String title;
    private String repeatType;
    private String repeatValue;
    private String categoryName;

    private Boolean isCompleted;

    private String createdValue;


    private String repeatText;


    private int progress;
    private Long categoryId;


    public static GoalListDTO fromEntity(Goal goal) {
        return new GoalListDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getRepeatType().name(),
                goal.getRepeatValue(),
                goal.getCategory() != null ? goal.getCategory().getName() : "미지정",
                goal.getIsCompleted(),
                goal.getCreatedValue().toString(),
                "", // repeatText 직접 생성하거나 로직 넣기
                goal.getProgress(),
                goal.getCategory() != null ? goal.getCategory().getId() : null
        );
    }

}
