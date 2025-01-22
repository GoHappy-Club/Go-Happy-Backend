package com.startup.goHappy.admin.services;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.controllers.EventController;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.integrations.model.ZoomParticipantsDTO;
import com.startup.goHappy.integrations.service.ZoomDataWriter;
import com.startup.goHappy.integrations.service.ZoomService;
import com.startup.goHappy.jobs.RewardsJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ZoomStats {
    @Autowired
    private ZoomDataWriter zoomDataWriter;
    @Autowired
    private EventController eventController;
    @Autowired
    private ZoomService zoomService;
    @Autowired
    private RewardsJob rewardsJob;

    public void getInfo(String tag,String startTime,String endTime) throws ExecutionException, InterruptedException {

        JSONObject params = new JSONObject();
        params.put("minDate",startTime);
        params.put("maxDate",endTime);

        List<Event> events_old = eventController.getEventsWithinDateRange(params);
        List<Event> events = new ArrayList<>();
        for (Event event : events_old) {
            if(event.getEventName().toLowerCase().contains(tag.toLowerCase())) {
                events.add(event);
            }
        }
        String zoomToken = zoomService.generateZoomOAuth();
        for (Event event : events) {
            String meetingNumber = rewardsJob.extractMeetingNumber(event.getMeetingLink());
            List<ZoomParticipantsDTO.Participant> participants = zoomService.getPastMeetingParticipants(meetingNumber,zoomToken);
            if (participants != null && !participants.isEmpty())
                zoomDataWriter.saveAsCsv(event.getId(), participants);
        }
    }

}
