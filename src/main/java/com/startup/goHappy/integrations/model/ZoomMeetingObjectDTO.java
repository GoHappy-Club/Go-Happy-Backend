package com.startup.goHappy.integrations.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ZoomMeetingObjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String uuid;

    private String assistant_id;

    private String host_email;

    private String registration_url;

    private String topic;

    private Integer type;

    private String start_time;

    private Integer duration;

    private String schedule_for;

    private String timezone;

    private String created_at;

    private String password;

    private String agenda;

    private String start_url;

    private String join_url;

    private String h323_password;

    private Integer pmi;

    private ZoomMeetingRecurrenceDTO recurrence;

    private List<ZoomMeetingTrackingFieldsDTO> tracking_fields;

    private List<ZoomMeetingOccurenceDTO> occurrences;

    private ZoomMeetingSettingsDTO settings;
}