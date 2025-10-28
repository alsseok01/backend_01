package org.hknu.Dto;

import lombok.Data;
import java.util.List;

@Data
public class PostRequest {
    private String title;
    private String content;
    private List<String> tags;
    private Double latitude;
    private Double longitude;
    private String address;
}