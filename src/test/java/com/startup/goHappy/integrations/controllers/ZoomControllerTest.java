package com.startup.goHappy.integrations.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.startup.goHappy.integrations.controller.ZoomController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.ZoomService;

public class ZoomControllerTest {

    @InjectMocks
    private ZoomController zoomController;

    @Mock
    private ZoomService zoomService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetup() throws IOException {
        JSONObject params = new JSONObject();
        params.put("topic", "Test Meeting");
        params.put("type", 2);
        params.put("duration", 60);
        params.put("start_time", "2023-04-10T10:00:00Z");

        ZoomMeetingObjectDTO zoomdto = new ZoomMeetingObjectDTO();
        zoomdto.setJoin_url("https://zoom.us/j/1234567890");

        when(zoomService.createMeeting(any(ZoomMeetingObjectDTO.class))).thenReturn(zoomdto);

        String joinUrl = zoomController.setup(params);

        assertEquals("https://zoom.us/j/1234567890", joinUrl);
    }

}
