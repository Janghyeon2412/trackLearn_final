package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NextScheduleDTO {

    private String date;
    private String dDay;
    private List<NextScheduleGoalDTO> goals;

}
