package com.startup.goHappy.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.integrations.service.ZoomService;

import io.micrometer.core.instrument.util.StringEscapeUtils;

@RestController
@RequestMapping("event")
public class EventController {
	
	
	private static DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' HH:mm:ss a");

	@Autowired
	EventRepository eventService;
	
	@Autowired
	ZoomService zoomService;
	
	@Autowired
	EmailService emailService;
	String content = "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\n"
			+ "		<tr>\n"
			+ "			<td align=\"center\" style=\"padding:0;\">\n"
			+ "				<table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\n"
			+ "					<tr>\n"
			+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;background:#70bbd9;\">\n"
			+ "							<img src=\"https://storage.googleapis.com/gohappy-main-bucket/logo.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "					<tr>\n"
			+ "						<td style=\"padding:36px 30px 42px 30px;\">\n"
			+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "								<tr>\n"
			+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\n"
			+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Thank you for choosing us.</h1>\n"
			+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Below are the details for your session: </p>\n"
			+ "                    <p><b>Title: </b>${title}</p>\n"
			+ "                    <p><b>Date & Time: </b>${date}</p>\n"
			+ "					   <a href=\"${zoomLink}\" target=\"_blank\" style=\"text-decoration: none;\"><button type=\"button\" style=\"background-color: #70bbd9;\n"
			+ "  border: none;\n"
			+ "  color: white;\n"
			+ "  padding: 15px 32px;cursor: pointer;\n"
			+ "  text-align: center;\n"
			+ "  text-decoration: none;\n"
			+ "  display: inline-block;\n"
			+ "  font-size: 16px;margin:0 auto;\n"
			+ "    display:block;\">Join Now</button></a> \n"
			+ "									</td>\n"
			+ "								</tr>\n"
			+ "								\n"
			+ "							</table>\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "					<tr>\n"
			+ "						<td style=\"padding:30px;background:#70bbd9;\">\n"
			+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\n"
			+ "								<tr>\n"
			+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\n"
			+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\n"
			+ "											&reg; GoHappy Club 2021<br/>\n"
			+ "										</p>\n"
			+ "									</td>\n"
			+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\n"
			+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "										</table>\n"
			+ "									</td>\n"
			+ "								</tr>\n"
			+ "							</table>\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "				</table>\n"
			+ "			</td>\n"
			+ "		</tr>\n"
			+ "	</table>";

	@SuppressWarnings("deprecation")
	@PostMapping("create")
	public void createEvent(@RequestBody JSONObject event) throws JsonMappingException, JsonProcessingException {
		//eventService.deleteAll();
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ObjectMapper objectMapper = new ObjectMapper();
		Event ev = objectMapper.readValue(event.toJSONString(), Event.class);	
		ev.setId(UUID.randomUUID().toString());
		ev.setParticipantList(new ArrayList<String>());

		ev.setType(StringUtils.isEmpty(event.getString("type"))?"0":event.getString("type"));
		if(!StringUtils.isEmpty(ev.getCron())) {
			TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
			CronSequenceGenerator generator = new CronSequenceGenerator(ev.getCron(),tz);
			Date nextExecutionDate = generator.next(Date.from(zonedDateTime.now().toInstant()));
			ev.setIsParent(true);
			ev.setEventDate("");
			eventService.save(ev);
			long duration = (Long.parseLong(ev.getEndTime())- nextExecutionDate.getTime())/(60000);
			int i=0;
			String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
//			Date d = new Date(ev.getStartTime());
			Event tempChild=null;
			while(i<Integer.parseInt(event.getString("occurance")) && nextExecutionDate!=null) {
				Event childEvent = objectMapper.readValue(event.toJSONString(), Event.class);
				childEvent.setId(UUID.randomUUID().toString());
				childEvent.setIsParent(false);
				childEvent.setParentId(ev.getId());
				childEvent.setIsScheduled(false);
				childEvent.setCron("");
				childEvent.setStartTime(""+nextExecutionDate.getTime());
				childEvent.setEventDate(""+nextExecutionDate.getTime());
				long start = nextExecutionDate.getTime();
				
				Date newEndTime = new Date(nextExecutionDate.getTime()+duration*60000);
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
		eventService.delete(eventService.get(id).get());
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<Event> events = eventService.retrieveAll();
		List<Event> result = IterableUtils.toList(events);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("getEventsByDate")
	public JSONObject getEventsByDate(@RequestBody JSONObject params){
		System.out.println(params.getString("date"));
		
		Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		zonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		System.out.println(zonedDateTime);

		System.out.println(zonedDateTime.toInstant().toEpochMilli());
		CollectionReference eventsRef = eventService.getCollectionReference();

		Query query1 = eventsRef.whereGreaterThan("startTime", params.getString("date"));
		Query query2 = null;
		if(params.getString("endDate")!=null) {
			query2 = eventsRef.whereLessThan("endTime", ""+params.getString("endDate"));
		}
		else {
			query2 = eventsRef.whereLessThan("endTime", ""+zonedDateTime.toInstant().toEpochMilli());
		}
		Query query3 = eventsRef.whereEqualTo("isParent", false);

		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();
		ApiFuture<QuerySnapshot> querySnapshot3 = query3.get();

		
		Set<Event> events1 = new HashSet<>();
		Set<Event> events2 = new HashSet<>();
		Set<Event> events3 = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
				events1.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
				events2.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
				events3.add(document.toObject(Event.class));  
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		events1.retainAll(events2);
		events1.retainAll(events3);
		List<Event> events = IterableUtils.toList(events1);
		Collections.sort(events,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		System.out.println(events.size());
		JSONObject output = new JSONObject();
		output.put("events", events);
		return output;
	}
	@PostMapping("bookEvent")
	public String bookEvent(@RequestBody JSONObject params) throws IOException, MessagingException, GeneralSecurityException {
		CollectionReference eventRef = eventService.getCollectionReference();
		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.get();
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
		Map<String, Object> map = new HashMap<>();
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		eventRef.document(params.getString("id")).update(map);
//		content = content.replace("${username}", event.getEventName());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(event.getEventDate()));
		
		
		content = content.replace("${title}", event.getEventName());
		content = content.replace("${zoomLink}", event.getMeetingLink());
		content = content.replace("${zoomLink}", event.getMeetingLink());
		content = content.replace("${date}", FOMATTER.format(((GregorianCalendar) calendar).toZonedDateTime()));
		emailService.sendSimpleMessage(params.getString("email"), "GoHappy Club: Session Booked", content);
		return "SUCCESS";
	}
	@PostMapping("cancelEvent")
	public String cancelEvent(@RequestBody JSONObject params) throws IOException {
		CollectionReference eventRef = eventService.getCollectionReference();
		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.get();

		event.setSeatsLeft(event.getSeatsLeft()+1);
		List<String> participants = event.getParticipantList();
		participants.remove(params.getString("email"));
		event.setParticipantList(participants);
		Map<String, Object> map = new HashMap<>();
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		eventRef.document(params.getString("id")).update(map);
		return "SUCCESS";
	}
	@PostMapping("mySessions")
	public JSONObject mySessions(@RequestBody JSONObject params) throws IOException {
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ZonedDateTime endZonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		CollectionReference eventsRef = eventService.getCollectionReference();
//		.whereArrayContains("participantList", params.getString("email")).whereEqualTo("isParent", false)
		Query query1 = eventsRef.whereGreaterThan("startTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("email")).whereEqualTo("isParent", false);
//		zonedDateTime.toInstant().toEpochMilli()
		Query query2 = eventsRef.whereLessThan("endTime", ""+endZonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("email")).whereEqualTo("isParent", false);
		
		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();

		
		Set<Event> events1 = new HashSet<>();
		Set<Event> events2 = new HashSet<>();
		Set<Event> events3 = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
				events1.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
				events2.add(document.toObject(Event.class));  
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		JSONObject paramsForEventByDate = new JSONObject();
		paramsForEventByDate.put("date", ""+zonedDateTime.toInstant().toEpochMilli());
		paramsForEventByDate.put("endDate", ""+zonedDateTime.toInstant().toEpochMilli());
		
		JSONObject ongoingJSON = getEventsByDate(paramsForEventByDate);
		JSONObject output = new JSONObject();
		ObjectMapper objectMapper = new ObjectMapper();
		for(Object obj:ongoingJSON.getJSONArray("events")) {
			Event ev =((Event)obj);
			if(ev.getParticipantList()!=null && ev.getParticipantList().size()>0 &&  ev.getParticipantList().contains(params.getString("email"))) {
				events3.add(ev);
			}
		}
		output.put("upcomingEvents", events1);
		output.put("expiredEvents", events2);
		output.put("ongoingEvents", events3);
		
		return output;
	}
}  

