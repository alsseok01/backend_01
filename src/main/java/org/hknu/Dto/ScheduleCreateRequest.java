package org.hknu.Dto;

import lombok.Data;

@Data
public class ScheduleCreateRequest {
    private String date;
    private Integer time;
    private String text;
    private String placeName;
    private String placeCategory;
    private Integer participants;
}