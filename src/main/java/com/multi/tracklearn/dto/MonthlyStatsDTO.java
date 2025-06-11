package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MonthlyStatsDTO {
    private List<Integer> dailyStudyTimes; // ex: [0, 20, 60, ...]
    private int entryCount;
    private double averageSatisfaction;
    private String bestDay;
    private int achievementRate;

    private String month;
}
