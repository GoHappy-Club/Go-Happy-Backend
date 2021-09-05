package com.startup.goHappy.integrations.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ZoomMeetingOccurenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String occurrence_id;
 
    private String start_time;

    private Integer duration;

    private String status;
}
