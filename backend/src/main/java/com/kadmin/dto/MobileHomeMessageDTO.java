package com.kadmin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Mobile home message card.
 */
@Data
@Builder
public class MobileHomeMessageDTO {
    private String id;
    private String title;
    private String desc;
    private String time;
    private String color;
    private String icon;
    private String badge;
    private String url;
    private String type;
}
