package com.startup.goHappy.jobs;

import com.alibaba.fastjson.JSONObject;
import com.google.cloud.firestore.CollectionReference;
import com.startup.goHappy.controllers.EventController;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.integrations.model.ZoomParticipantsDTO;
import com.startup.goHappy.integrations.service.ZoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RewardsJob {
    @Autowired
    EventRepository eventService;

    @Autowired
    ZoomService zoomService;
    @Autowired
    private EventController eventController;

//    @Scheduled(cron = "0 30 03 * * *", zone = "Asia/Kolkata")
    public void getParticipantInfo() throws ExecutionException, InterruptedException, IOException {
        long now = new Date().getTime();
        long twentyFourHoursAgo = now - (24 * 60 * 60 * 1000);

        JSONObject params = new JSONObject();
        params.put("minDate", "" + twentyFourHoursAgo);
        params.put("maxDate", "" + now);

        List<Event> events = eventController.getEventsWithinDateRange(params);
        for (Event event : events) {
            String meetingNumber = extractMeetingNumber(event.getMeetingLink());
            List<ZoomParticipantsDTO.Participant> participants = zoomService.getPastMeetingParticipants(meetingNumber);

            HashMap<String, Integer> phoneDurationMap = new HashMap<>();
            int maxDuration = 0;

            for (ZoomParticipantsDTO.Participant participant : participants) {
                String phoneNumber = participant.getCustomer_key();
                int durationInMinutes = participant.getDuration() / 60;

                if (!phoneNumber.isEmpty()) {
                    phoneDurationMap.put(phoneNumber, phoneDurationMap.getOrDefault(phoneNumber, 0) + durationInMinutes);
                }
                maxDuration = Math.max(maxDuration, phoneDurationMap.getOrDefault(phoneNumber, 0) + durationInMinutes);
            }
            double threshold = maxDuration * 0.6;
            for (Map.Entry<String, Integer> entry : phoneDurationMap.entrySet()) {
                String phoneNumber = entry.getKey();
                int duration = entry.getValue();
                if (duration >= threshold) {
                    JSONObject giveRewardParams = new JSONObject();
                    giveRewardParams.put("phoneNumber", phoneNumber);
                    giveRewardParams.put("eventId", event.getId());
                    eventController.giveReward(giveRewardParams);
                }
            }
        }
    }

    private Map<String, Long> prepareReport() {
        Map<String, Long> map = new HashMap<>();
        return map;
    }

    private Map<String, String> processAttendanceData(String phoneNumber) {
        Map<String, String> attendanceData = new HashMap<>();
        attendanceData.put("phoneNumber", phoneNumber);
        attendanceData.put("duration", "50");
        return attendanceData;
    }

    public String extractMeetingNumber(String zoomLink) {
        String regex = "j\\/(\\d+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zoomLink);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public void storeAttendanceData(String eventId, List<ZoomParticipantsDTO.Participant> zoomData) {
        CollectionReference eventRef = eventService.getCollectionReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("zoomAnalytics", zoomData);
        updates.put("lastUpdated", com.google.cloud.Timestamp.now());
        eventRef.document(eventId).update(updates);
    }
}
