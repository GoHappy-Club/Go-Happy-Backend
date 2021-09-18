package com.startup.goHappy.integrations.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.ZoomService;

@RestController
@RequestMapping("zoom")
public class ZoomController {
	
	@Autowired
	ZoomService zoomService;
	
	@PostMapping("setup")
	public String setup(@RequestBody JSONObject params) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		
		ZoomMeetingObjectDTO zoomdto = objectMapper.readValue(params.toJSONString(), ZoomMeetingObjectDTO.class);	
		zoomdto = zoomService.createMeeting(zoomdto);
		return zoomdto.getJoin_url();
	}
	
	@PostMapping("getRecording")
	public String getRecording(@RequestBody JSONObject params) throws IOException {
		String recordingLink = zoomService.getRecordingById(params.getString("meetingId"));
		return recordingLink;
	}
}
