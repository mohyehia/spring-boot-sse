package com.mohyehia.sse.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notification {
    private String from;
    private String message;
}
