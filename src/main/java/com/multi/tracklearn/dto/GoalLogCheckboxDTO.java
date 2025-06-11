package com.multi.tracklearn.dto;

import com.multi.tracklearn.domain.GoalLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GoalLogCheckboxDTO {
    private Long id;
    private String title;
    private boolean checked;

    public static GoalLogCheckboxDTO fromEntity(GoalLog goalLog) {
        return new GoalLogCheckboxDTO(
                goalLog.getId(),
                goalLog.getGoal().getTitle(),
                goalLog.isChecked()
        );
    }
}