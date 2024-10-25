package com.startup.goHappy.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import com.startup.goHappy.entities.TypecastedModels.SearchEventDTO;
import com.startup.goHappy.entities.model.*;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.*;
import com.startup.goHappy.enums.MembershipEnum;
import com.startup.goHappy.utils.Constants;
import com.startup.goHappy.utils.Helpers;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.integrations.service.ZoomService;
import com.startup.goHappy.utils.TambolaGenerator;


@RestController
@RequestMapping("/event")
public class EventController {
	
	
	private static DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' HH:mm:ss a");

	@Autowired
	EventRepository eventService;

	@Autowired
	ReferralRepository referralService;
	
	@Autowired
	ZoomService zoomService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	TambolaGenerator tambolaGenerator;
	
	@Autowired
	UserProfileController userProfileController;
	
	@Autowired
	UserProfileRepository userProfileService;

	@Autowired
	PaymentLogRepository paymentLogService;

	@Autowired
	MembershipController membershipController;

	@Autowired
	UserMembershipsRepository userMembershipsService;

	@Autowired
	Helpers helpers;

	@Autowired
	Constants constants;


	public long calculateDuration(long t1, long t2){
		ZonedDateTime zonedDateTime1 = java.time.ZonedDateTime
				.ofInstant(java.time.Instant.ofEpochMilli(t1),java.time.ZoneId.of("Asia/Kolkata"));
		ZonedDateTime zonedDateTime2 = java.time.ZonedDateTime
				.ofInstant(java.time.Instant.ofEpochMilli(t2),java.time.ZoneId.of("Asia/Kolkata"));
		return Math.abs(java.time.temporal.ChronoUnit.MINUTES.between(zonedDateTime1.toLocalTime(),zonedDateTime2.toLocalTime()));
	}

	@SuppressWarnings("deprecation")
	@ApiOperation(value = "To create an event (can be a single or a recurring event)")
	@PostMapping("/create")
	public void createEvent(@RequestBody JSONObject event) throws IOException {
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ObjectMapper objectMapper = new ObjectMapper();
		Event ev = objectMapper.readValue(event.toJSONString(), Event.class);	
		ev.setId(UUID.randomUUID().toString());
		ev.setParticipantList(new ArrayList<String>());
		ev.setCostType((!StringUtils.isEmpty(event.getString("costType")) && event.getString("costType").equals("paid"))?"paid":"free");
		ev.setType(StringUtils.isEmpty(event.getString("type"))?"session":event.getString("type"));
		if(event.getString("eventName").toLowerCase().contains("tambola")) {
			List<Integer> numberCaller = new ArrayList<>();
			Map<String,Integer> liveTambola = new HashMap<>();
			for (int i = 1; i <= 90; i++) {
				numberCaller.add(i);
			}
			Collections.shuffle(numberCaller);
			liveTambola.put("index",-1);
			liveTambola.put("value",null);
			liveTambola.put("lastNumber",null);
			ev.setTambolaNumberCaller(numberCaller);
			ev.setLiveTambola(liveTambola);
		}
		if(!StringUtils.isEmpty(ev.getCron())) {
			TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
			CronSequenceGenerator generator = new CronSequenceGenerator(ev.getCron(),tz);
			Date nextExecutionDate = generator.next(Date.from(zonedDateTime.now().toInstant()));
			ev.setIsParent(true);
			ev.setEventDate("");
			eventService.save(ev);
			long duration = calculateDuration(Long.parseLong(ev.getEndTime()), nextExecutionDate.getTime());
			//long duration = (Long.parseLong(ev.getEndTime())- nextExecutionDate.getTime())/(60000);

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
				System.out.println(duration);
				Date newEndTime = new Date(nextExecutionDate.getTime()+duration*60000);
				childEvent.setEndTime(""+newEndTime.getTime());

				if(event.getString("eventName").toLowerCase().contains("tambola")) {
					List<Integer> numberCaller = new ArrayList<>();
					Map<String,Integer> liveTambola = new HashMap<>();
					for (int k = 1; k <= 90; k++) {
						numberCaller.add(k);
					}
					Collections.shuffle(numberCaller);
					liveTambola.put("index",-1);
					liveTambola.put("value",null);
					liveTambola.put("lastNumber",null);
					childEvent.setTambolaNumberCaller(numberCaller);
					childEvent.setLiveTambola(liveTambola);
				}
				
				ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
				obj.setTopic(ev.getEventName());
				obj.setTimezone("Asia/Kolkata");
				obj.setDuration((int)duration);
				obj.setType(2);
				obj.setPassword("12345");

				DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
				Instant startTimeInstance = Instant.ofEpochMilli(start);
				LocalDateTime localDateTime = LocalDateTime
						.ofInstant(startTimeInstance, ZoneId.of("Asia/Kolkata"));
				String finalDateForZoom = newYorkDateFormatter.format(ZonedDateTime.of(localDateTime, ZoneId.of("Asia/Kolkata")));
				finalDateForZoom = finalDateForZoom.replace(" ", "T");

				obj.setStart_time(finalDateForZoom);
				ZoomMeetingObjectDTO zoomData = zoomService.createMeeting(obj);
				String zoomLink = zoomData.getJoin_url();
				Long meetingId = zoomData.getId();

				childEvent.setMeetingLink(zoomLink);
				childEvent.setMeetingId(""+meetingId);
				eventService.save(childEvent);
				tempChild=childEvent;
				nextExecutionDate = generator.next(nextExecutionDate);
				i++;
				
			}
		}
		else {
			long duration = (Long.parseLong(ev.getEndTime())- Long.parseLong(ev.getStartTime()))/(60000);
			ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
			obj.setTopic(ev.getEventName());
			obj.setTimezone("Asia/Kolkata");
			obj.setDuration((int)duration);
			obj.setType(2);
			obj.setPassword("12345");


			String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
			DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
			Instant startTimeInstance = Instant.ofEpochMilli(Long.parseLong(ev.getStartTime()));
			LocalDateTime localDateTime = LocalDateTime
					.ofInstant(startTimeInstance, ZoneId.of("Asia/Kolkata"));
			String finalDateForZoom = newYorkDateFormatter.format(ZonedDateTime.of(localDateTime, ZoneId.of("Asia/Kolkata")));
			finalDateForZoom = finalDateForZoom.replace(" ", "T");

			obj.setStart_time(finalDateForZoom);
			
			ZoomMeetingObjectDTO zoomData = zoomService.createMeeting(obj);
			
			String zoomLink = zoomData.getJoin_url();
			Long meetingId = zoomData.getId();
			
			ev.setMeetingLink(zoomLink);
			ev.setMeetingId(""+meetingId);
			eventService.save(ev);
		}
		
		return;
	}

	@ApiOperation(value = "To delete an event")
	@PostMapping("delete")
	public void deleteEvent(@RequestBody JSONObject params) {
		String id = params.getString("id");
		eventService.delete(eventService.get(id).get());
		return;
	}

	@ApiOperation(value = "To search events")
	@GetMapping("searchEvents")
	public List<SearchEventDTO> searchEvents(@RequestParam String inputSearch) throws IOException, ExecutionException, InterruptedException {
		long startEpoch = new Date().getTime();
		long endEpoch = startEpoch + (14 * 24 * 60 * 60 * 1000); // 14 days later
		String startInstance = String.valueOf(startEpoch);
		String endInstance = String.valueOf(endEpoch);
		CollectionReference eventRef = eventService.getCollectionReference();
        Query query = eventRef.select("eventName", "expertName", "expertImage", "startTime", "coverImage", "id", "participantList", "eventDate");
		query = query.whereGreaterThanOrEqualTo("endTime", startInstance).whereLessThanOrEqualTo("startTime", endInstance);
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		List<SearchEventDTO> events = new ArrayList<>();

		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			SearchEventDTO event = document.toObject(SearchEventDTO.class);
            assert event != null;
            if (helpers.matches(event.getExpertName().toLowerCase(), inputSearch.toLowerCase()) || helpers.matches(event.getEventName().toLowerCase(), inputSearch.toLowerCase())) {
				events.add(event);
			}
		}
		return events;
	}

	@ApiOperation(value = "To get all the events (not recommended to run)")
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<Event> events = eventService.retrieveAll();
		List<Event> result = IterableUtils.toList(events);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@ApiOperation(value = "Get events by date range (used when user clicks a date on the app)")
	@PostMapping("getEventsByDate")
	public JSONObject getEventsByDate(@RequestBody JSONObject params){
		System.out.println("date"+params.getString("date"));
		
		Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		zonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		System.out.println(zonedDateTime);

		System.out.println(zonedDateTime.toInstant().toEpochMilli());
		CollectionReference eventsRef = eventService.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime2 = java.time.ZonedDateTime
		                            .ofInstant(instance2,java.time.ZoneId.of("Asia/Kolkata"));
		String newDate = zonedDateTime2.toInstant().toEpochMilli()+"";
//		if(params.getString("date").compareTo(newDate)<0) {
//			params.put("date", newDate);
//		}
		Query queryNew = eventsRef.whereGreaterThanOrEqualTo("startTime", params.getString("date")).whereEqualTo("isParent",false);

		if(params.getString("endDate")!=null) {
			queryNew = queryNew.whereLessThanOrEqualTo("endTime", ""+params.getString("endDate"));
		}
		else {
			queryNew = queryNew.whereLessThanOrEqualTo("endTime", ""+zonedDateTime.toInstant().toEpochMilli());
		}
		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Event> eventsNew = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				eventsNew.add(document.toObject(Event.class));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		List<Event> eventsNewBest = IterableUtils.toList(eventsNew);

		Collections.sort(eventsNewBest,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		JSONObject output = new JSONObject();
		output.put("events", eventsNewBest);
		return output;
	}

	@ApiOperation(value = "Get events by within date range")
	@PostMapping("getEventsWithinDateRange")
	public List<Event> getEventsWithinDateRange(@RequestBody JSONObject params){
		CollectionReference eventsRef = eventService.getCollectionReference();
		Query queryNew = eventsRef.whereGreaterThanOrEqualTo("startTime", params.getString("minDate")).whereEqualTo("isParent",false);
		queryNew = queryNew.whereLessThanOrEqualTo("startTime", params.getString("maxDate"));

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Event> eventsNew = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				eventsNew.add(document.toObject(Event.class));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		List<Event> eventsNewBest = IterableUtils.toList(eventsNew);

		Collections.sort(eventsNewBest,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		return eventsNewBest;
	}

	@ApiOperation(value = "Get event by ID")
	@PostMapping("getEvent")
	public JSONObject getEventById(@RequestBody JSONObject params){
		String id = params.getString("id");

		CollectionReference eventsRef = eventService.getCollectionReference();
		Query query = eventsRef.whereEqualTo("id", id);

		ApiFuture<QuerySnapshot> querySnapshotNew = query.get();

		Event event = null;
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				event = document.toObject(Event.class);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		JSONObject output = new JSONObject();
		output.put("event", event);
		return output;
	}

	@ApiOperation(value = "To book an event")
	@PostMapping("bookEvent")
	public String bookEvent(@RequestBody JSONObject params) throws IOException, MessagingException, GeneralSecurityException, InterruptedException, ExecutionException {
		System.out.println("RUnning book");
		CollectionReference eventRef = eventService.getCollectionReference();
		CollectionReference referrals = referralService.getCollectionReference();

		JSONObject getMembershipByPhoneParams = new JSONObject();
		getMembershipByPhoneParams.put("phone", params.getString("phoneNumber"));
		UserMemberships userMembership= membershipController.getMembershipByPhone(getMembershipByPhoneParams);

		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.orElse(null);
		if (event == null) {
			return "FAILED: EVENT NOT FOUND";
		}
		String ticket = "\""+params.getString("tambolaTicket")+"\"";
		String result = isParticipationAllowed(event,userMembership);
        if(!result.equals("SUCCESS")){
            return result;
        }
		event.setSeatsLeft(event.getSeatsLeft()-1);
		List<String> participants = event.getParticipantList();
		if(participants==null) {
			participants = new ArrayList<String>();
		}
		participants.add(params.getString("phoneNumber"));
		
		event.setParticipantList(participants);

		// deduct coins from user's wallet if paid event
		if (StringUtils.equals(event.getCostType(), "paid")) {
			userMembership.setCoins(userMembership.getCoins() - event.getCost());
			userMembershipsService.save(userMembership);
		}

		//TAMBOLA GENERATION-START
		List<String> tambolaTickets = event.getTambolaTickets();
		if(tambolaTickets==null) {
			tambolaTickets = new ArrayList<String>();
		}
		if(event.getEventName().toLowerCase().contains("tambola")) {
			tambolaTickets.add(ticket);
		}
		
		event.setTambolaTickets(tambolaTickets);
		//TAMBOLA GENERATION-END
		
		Map<String, Object> map = new HashMap<>();
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		map.put("tambolaTickets",tambolaTickets);
		eventRef.document(params.getString("id")).update(map);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(event.getStartTime()));
		calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
		String currentContent = constants.getEventContent();
		currentContent = currentContent.replace("${title}", event.getEventName());
		currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${meetingId}", event.getMeetingId()!=null?event.getMeetingId():"");
		//content = content.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${date}", FOMATTER.format(((GregorianCalendar) calendar).toZonedDateTime()));
		UserProfile user = userProfileController.getUserByPhone(params).getObject("user", UserProfile.class);
		if(user!=null) {
			currentContent = currentContent.replace("${name}",user.getName());
			currentContent = currentContent.replace("${name}",user.getName());
			Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
			sessionsAttended++;
			user.setSessionsAttended(""+sessionsAttended);
			userProfileService.save(user);
			if(!StringUtils.isEmpty(user.getEmail()))
				emailService.sendSimpleMessage(user.getEmail(), event.getEventName(), currentContent);
		}

		return "SUCCESS";
	}

	@ApiOperation(value = "To cancel an event")
	@PostMapping("cancelEvent")
	public String cancelEvent(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException {
		System.out.println("RUnning cancel");
		CollectionReference eventRef = eventService.getCollectionReference();

		JSONObject getMembershipByPhoneParams = new JSONObject();
		getMembershipByPhoneParams.put("phone",params.getString("phoneNumber"));
		UserMemberships userMembership= membershipController.getMembershipByPhone(getMembershipByPhoneParams);
		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.orElse(null);
		if (event == null) {
			return "FAILED: EVENT NOT FOUND";
		}

		event.setSeatsLeft(event.getSeatsLeft()+1);
		List<String> participants = event.getParticipantList();
		int index = participants.indexOf(params.getString("phoneNumber"));
		participants.set(index,null);
		event.setParticipantList(participants);

		// refund coins to user's wallet in case of paid event
		if (StringUtils.equals(event.getCostType(),"paid")) {
			userMembership.setCoins(userMembership.getCoins() + event.getCost());
			userMembershipsService.save(userMembership);
		}

		Map<String, Object> map = new HashMap<>();
		if(event.getEventName().toLowerCase().contains("tambola")) {
			List<String> tickets = event.getTambolaTickets();
			if(tickets!=null) {
//				tickets.remove(index);
				tickets.set(index,null);
				event.setTambolaTickets(tickets);
				map.put("tambolaTickets",tickets);
			}
		}
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		eventRef.document(params.getString("id")).update(map);
		try {
			JSONObject userJson = userProfileController.getUserByPhone(params);
			UserProfile user = userJson.getObject("user", UserProfile.class);
			Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
			sessionsAttended--;
			user.setSessionsAttended(""+sessionsAttended);
			userProfileService.save(user);
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "SUCCESS";
	}
	@ApiOperation(value = "Get list of past sessions attended by a user")
	@PostMapping("mySessions")
	public JSONObject mySessions(@RequestBody JSONObject params) throws IOException {
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ZonedDateTime endZonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		CollectionReference eventsRef = eventService.getCollectionReference();
//		.whereArrayContains("participantList", params.getString("email")).whereEqualTo("isParent", false)
//		Query query1 = eventsRef.whereGreaterThan("startTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("phoneNumber")).whereEqualTo("isParent", false);
//		zonedDateTime.toInstant().toEpochMilli()
		Query query2 = eventsRef.whereLessThan("endTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("phoneNumber")).whereEqualTo("isParent", false);
		
//		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();

		
//		Set<Event> events1 = new HashSet<>();
		Set<Event> events2 = new HashSet<>();
//		Set<Event> events3 = new HashSet<>();
		try {
//			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
//				events1.add(document.toObject(Event.class));
//			}
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
		
//		JSONObject ongoingJSON = getOngoingEvents(paramsForEventByDate);
		JSONObject output = new JSONObject();
		ObjectMapper objectMapper = new ObjectMapper();
//		for(Object obj:ongoingJSON.getJSONArray("events")) {
//			Event ev =((Event)obj);
//			if(ev.getParticipantList()!=null && ev.getParticipantList().size()>0 &&  ev.getParticipantList().contains(params.getString("phoneNumber"))) {
//				events3.add(ev);
//			}
//		}
//		events1.removeAll(events3);
//		ArrayList<Event> events1List = new ArrayList<>(events1);
		ArrayList<Event> events2List = new ArrayList<>(events2);
//		Collections.sort(events1List,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		Collections.sort(events2List,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		Collections.reverse(events2List);
		output.put("upcomingEvents", new ArrayList());
		output.put("expiredEvents", events2List.subList(0,events2List.size()>12?12:events2List.size()));
		output.put("ongoingEvents", new ArrayList());

		return output;
	}
	public JSONObject getOngoingEvents(JSONObject params){
		System.out.println(params.getString("date"));

		Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		zonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		System.out.println(zonedDateTime);

		System.out.println(zonedDateTime.toInstant().toEpochMilli());
		CollectionReference eventsRef = eventService.getCollectionReference();
		Long newStartTime = Long.parseLong(params.getString("date"))+600000;
		Long newEndTime = Long.parseLong(params.getString("endDate"))+600000;

		Query query1 = eventsRef.whereLessThan("startTime", ""+newStartTime);
		Query query2 = eventsRef.whereGreaterThan("endTime", ""+newEndTime);
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

    private String isParticipationAllowed(Event event, UserMemberships membership) {
        if (event.getSeatsLeft() <= 0) {
            return "FAILED:FULL";
        }
        if (StringUtils.equals(event.getCostType(), "paid")) {
            if (membership.getMembershipType() == MembershipEnum.Free) {
                return "FAILED:NON-MEMBER";
            } else if (event.getCost() > membership.getCoins()) {
                return "FAILED:COST";
            }
        }
        return "SUCCESS";
    }
}  

