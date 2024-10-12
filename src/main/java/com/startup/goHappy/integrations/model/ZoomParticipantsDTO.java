package com.startup.goHappy.integrations.model;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ZoomParticipantsDTO {
    private String nextPageToken;
    private int pageCount;
    private int pageSize;
    private int totalRecords;
    private List<Participant> participants;

    @Data
    public static class Participant {
        private String customer_key;
        private int duration;
        private boolean failover;
        private String id;
        private String join_time;
        private String leave_time;
        private String name;
        private String registrant_id;
        private ParticipantStatus status;
        private String user_email;
        private String user_id;
        private String bo_mtg_id;
        private String participant_user_id;
    }

    public enum ParticipantStatus {
        @JsonProperty("in_meeting")
        IN_MEETING,

        @JsonProperty("in_waiting_room")
        IN_WAITING_ROOM
    }
}