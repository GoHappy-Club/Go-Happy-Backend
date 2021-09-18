package com.startup.goHappy.controllers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.service.EventService;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.ZoomService;

import io.micrometer.core.instrument.util.StringEscapeUtils;

@RestController
@RequestMapping("event")
public class EventController {

	@Autowired
	EventService eventService;
	
	@Autowired
	ZoomService zoomService;

	@SuppressWarnings("deprecation")
	@PostMapping("create")
	public void createEvent(@RequestBody JSONObject event) throws JsonMappingException, JsonProcessingException {
		//eventService.deleteAll();
		ObjectMapper objectMapper = new ObjectMapper();
		
		Event ev = objectMapper.readValue(event.toJSONString(), Event.class);	
		ev.setId(UUID.randomUUID().toString());
		ev.setParticipantList(new ArrayList<String>());

		ev.setType(StringUtils.isEmpty(event.getString("type"))?"0":event.getString("type"));
		if(!StringUtils.isEmpty(ev.getCron())) {
			CronSequenceGenerator generator = new CronSequenceGenerator(ev.getCron());
			Date nextExecutionDate = generator.next(new Date());
			ev.setIsParent(true);
			ev.setEventDate("");
			eventService.save(ev);
			long duration = (Long.parseLong(ev.getEndTime())- nextExecutionDate.getTime())/(60000);
			int i=0;
			String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
			//Date d = new Date(ev.getStartTime());
			Event tempChild=null;
			while(i<5 && nextExecutionDate!=null) {
				Event childEvent = objectMapper.readValue(event.toJSONString(), Event.class);
				childEvent.setId(UUID.randomUUID().toString());
				childEvent.setIsParent(false);
				childEvent.setParentId(ev.getId());
				childEvent.setIsScheduled(false);
				childEvent.setCron("");
				childEvent.setStartTime(""+nextExecutionDate.getTime());
				childEvent.setEventDate(""+nextExecutionDate.getTime());
				long start = nextExecutionDate.getTime();
				
				Date newEndTime = new Date(nextExecutionDate.getTime()+60000);
//				nextExecutionDate.setDate(parentEndTime.getDate());
//				nextExecutionDate.setMonth(parentEndTime.getMonth());
//				nextExecutionDate.setYear(parentEndTime.getYear());
//				nextExecutionDate.setHours(parentEndTime.getHours());
//				nextExecutionDate.setMinutes(parentEndTime.getMinutes());
//				nextExecutionDate.setSeconds(parentEndTime.getSeconds());
				childEvent.setEndTime(""+newEndTime.getTime());				
				
				ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
				obj.setTopic(ev.getEventName());
				obj.setTimezone("Asia/Kolkata");
				obj.setDuration((int)duration);
				obj.setType(2);
				obj.setPassword("212121");
				Date startDate = new Date(start);
				
				DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
				LocalDateTime summerDay = LocalDateTime.of(startDate.getYear()+1900, startDate.getMonth()+1, startDate.getDate(), startDate.getHours(), startDate.getMinutes());
				String finalDateForZoom = newYorkDateFormatter.format(ZonedDateTime.of(summerDay, ZoneId.of("Asia/Kolkata")));
				finalDateForZoom = finalDateForZoom.replace(" ", "T");

				obj.setStart_time(finalDateForZoom);
				
				String zoomLink = zoomService.createMeeting(obj).getJoin_url();
				
				childEvent.setMeetingLink(zoomLink);
				eventService.save(childEvent);
				tempChild=childEvent;
				nextExecutionDate = generator.next(nextExecutionDate);
				i++;
				
			}
		}
		else {
			eventService.save(ev);
		}
		
		return;
	}
	@PostMapping("delete")
	public void deleteEvent(@RequestBody JSONObject params) {
		String id = params.getString("id");
		eventService.delete(id);
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<Event> events = eventService.findAll();
		List<Event> result = IterableUtils.toList(events);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("getEventsByDate")
	public JSONObject getEventsByDate(@RequestBody JSONObject params) throws IOException {
		QueryBuilder qb = new BoolQueryBuilder();
		Date filterEnd = new Date(Long.parseLong(params.getString("date")));
		filterEnd.setHours(23);
		filterEnd.setMinutes(59);
		filterEnd.setSeconds(59);
		
		qb = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("startTime").gt(params.getString("date")))
				.must(QueryBuilders.rangeQuery("endTime").lt(""+filterEnd.getTime()))
				.mustNot(QueryBuilders.matchQuery("isParent",true));;
//		qb = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("endTime").lt(""+filterEnd.getTime()));
//		qb = QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery("isParent",true));
		
		Iterable<Event> events = eventService.search(qb);
		List<Event> result = IterableUtils.toList(events);
		Collections.sort(result,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("bookEvent")
	public String bookEvent(@RequestBody JSONObject params) throws IOException {
		Event event = eventService.findById(params.getString("id"));
		if(event.getSeatsLeft()<=0) {
			return "FAILED:FULL";
		}
		event.setSeatsLeft(event.getSeatsLeft()-1);
		List<String> participants = event.getParticipantList();
		if(participants==null) {
			participants = new ArrayList<String>();
		}
		participants.add(params.getString("email"));
		event.setParticipantList(participants);
		eventService.save(event);
		return "SUCCESS";
	}
	@PostMapping("cancelEvent")
	public String cancelEvent(@RequestBody JSONObject params) throws IOException {
		Event event = eventService.findById(params.getString("id"));
		event.setSeatsLeft(event.getSeatsLeft()+1);
		List<String> participants = event.getParticipantList();
		if(participants==null) {
			return "SUCCESS";
		}
		participants.remove(params.getString("email"));
		event.setParticipantList(participants);
		eventService.save(event);
		return "SUCCESS";
	}
	@PostMapping("mySessions")
	public JSONObject mySessions(@RequestBody JSONObject params) throws IOException {
		QueryBuilder upcomingQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantList.keyword",params.getString("email")))
				.must(QueryBuilders.rangeQuery("startTime").gte(""+new Date().getTime()));
		Iterable<Event> upcomingEvents = eventService.search(upcomingQb);
		List<Event> upresult = IterableUtils.toList(upcomingEvents);
		Collections.sort(upresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		
		QueryBuilder ongoingQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantList.keyword",params.getString("email")))
				.must(QueryBuilders.rangeQuery("startTime").lte(""+new Date().getTime()))
				.must(QueryBuilders.rangeQuery("endTime").gte(""+new Date().getTime()));
		Iterable<Event> ongoingEvents = eventService.search(ongoingQb);
		List<Event> ogresult = IterableUtils.toList(ongoingEvents);
		Collections.sort(ogresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		

		QueryBuilder expiredQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantList.keyword",params.getString("email")))
		.must(QueryBuilders.rangeQuery("endTime").lte(""+new Date().getTime()));
		Iterable<Event> expiredEvents = eventService.search(expiredQb);
		List<Event> expresult = IterableUtils.toList(expiredEvents);
		Collections.sort(expresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));

		
		JSONObject output = new JSONObject();
		output.put("upcomingEvents", upresult);
		output.put("ongoingEvents", ogresult);
		output.put("expiredEvents", expresult);
		return output;
	}
}  

