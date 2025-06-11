package com.multi.tracklearn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class GptStatsDTO {
    private Map<String, Integer> typeCounts;     // cheer, advice, adjust
    private List<String> recentFeedbacks;        // 최근 피드백 내용 리스트
}
