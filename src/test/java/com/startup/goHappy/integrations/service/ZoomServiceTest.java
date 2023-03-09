package com.startup.goHappy.integrations.service;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ZoomServiceTest {

    ZoomService zoomService;

    @Mock
    RestTemplate restTemplate;

    @Mock
    ResponseEntity<JSONObject> responseEntity;
    @Mock
    JSONObject jsonObject;

    @Mock
    ResponseEntity<ZoomMeetingObjectDTO> responsezmodCMEntity;

    @Mock
    ZoomMeetingObjectDTO zoommeetingobjectDTO;

    @Mock
    ResponseEntity<ZoomMeetingObjectDTO> responseZMODEntity;

    @Mock
    ZoomMeetingObjectDTO zoommeetingObjectDTO;

    @BeforeEach
    public void setup() {
        zoomService = new ZoomService(restTemplate);
        ReflectionTestUtils.setField(zoomService, "zoomUserId", "testUser");
        ReflectionTestUtils.setField(zoomService, "zoomApiKey", "testKey");
        ReflectionTestUtils.setField(zoomService, "zoomApiSecret", "testSecret");
//        ReflectionTestUtils.setField(zoomService, "accountId", "testAccount");
//        ReflectionTestUtils.setField(zoomService, "yourPass", "testPass");
    }


    @Test
    public void shouldReturnRecordingGivenId() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<JSONObject>> any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCodeValue()).thenReturn(200);
        when(responseEntity.getBody()).thenReturn(jsonObject);
        when(jsonObject.getString(Mockito.anyString())).thenReturn("rec001234");
        String result = zoomService.getRecordingById("testMeetingId");
        assertNotNull(result);
        assertEquals("rec001234", result);
    }

    @Test
    public void shouldReturn400WhenRecordingGivenId() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<JSONObject>> any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCodeValue()).thenReturn(400);
        String result = zoomService.getRecordingById("testMeetingId");
        assertNull(result);
    }

    @Test
    public void shouldReturn404WhenRecordingGivenId() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<JSONObject>> any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCodeValue()).thenReturn(404);
        String result = zoomService.getRecordingById("testMeetingId");
        assertNull(result);
    }

    @Test
    public void shouldcreateMeeting() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responsezmodCMEntity);
        when(responsezmodCMEntity.getStatusCodeValue()).thenReturn(201);
        when(responsezmodCMEntity.getBody()).thenReturn(zoommeetingobjectDTO);
        ZoomMeetingObjectDTO result = zoomService.createMeeting(zoommeetingobjectDTO);
        assertNotNull(result);
    }

    @Test
    public void should200createMeeting() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responsezmodCMEntity);
        when(responsezmodCMEntity.getStatusCodeValue()).thenReturn(200);
        ZoomMeetingObjectDTO result = zoomService.createMeeting(zoommeetingobjectDTO);
        assertNotNull(result);
    }

    @Test
    public void should400createMeeting() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responsezmodCMEntity);
        when(responsezmodCMEntity.getStatusCodeValue()).thenReturn(400);
        ZoomMeetingObjectDTO result = zoomService.createMeeting(zoommeetingobjectDTO);
        assertNotNull(result);
    }

    @Test
    public void should404createMeeting() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responsezmodCMEntity);
        when(responsezmodCMEntity.getStatusCodeValue()).thenReturn(404);
        ZoomMeetingObjectDTO result = zoomService.createMeeting(zoommeetingobjectDTO);
        assertNotNull(result);
    }

    @Test
    public void ZoomMeetingObjectDTO(){
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responseZMODEntity);
        when(responseZMODEntity.getStatusCodeValue()).thenReturn(200);
        when(responseZMODEntity.getBody()).thenReturn(zoommeetingObjectDTO);
        ZoomMeetingObjectDTO result = zoomService.getZoomMeetingById("testZoomMeetingObjectDTO");
        assertNotNull(result);
    }

    @Test
    public void ZoomMeeting400ObjectDTO() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responseZMODEntity);
        when(responseZMODEntity.getStatusCodeValue()).thenReturn(400);
        ZoomMeetingObjectDTO result = zoomService.getZoomMeetingById("testZoomMeetingObjectDTO");
        assertNull(result);
    }

    @Test
    public void ZoomMeeting404ObjectDTO() {
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Matchers.<Class<ZoomMeetingObjectDTO>> any())).thenReturn(responseZMODEntity);
        when(responseZMODEntity.getStatusCodeValue()).thenReturn(404);
        ZoomMeetingObjectDTO result = zoomService.getZoomMeetingById("testZoomMeetingObjectDTO");
        assertNull(result);
    }

}
