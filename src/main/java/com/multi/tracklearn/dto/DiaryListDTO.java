package com.multi.tracklearn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryListDTO {

    private Long id;
    private String title;
    private String summary;
    private String date;
    private String tags;
    private float satisfaction;

    @JsonProperty("isFavorite")
    private boolean isFavorite;

    private String content;

    private List<Long> goalLogIds;


}
