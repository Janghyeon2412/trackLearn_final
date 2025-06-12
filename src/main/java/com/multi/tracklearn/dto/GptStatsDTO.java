package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class GptStatsDTO {
    private Map<String, Integer> typeCounts;
    private List<String> recentFeedbacks;
}
