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
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.TypecastedModels.SearchEventDTO;
import com.startup.goHappy.entities.model.*;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.*;
import com.startup.goHappy.enums.MembershipEnum;
import com.startup.goHappy.enums.TransactionTypeEnum;
import com.startup.goHappy.enums.VoucherStatusEnum;
import com.startup.goHappy.services.RatingHelperService;
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
    CoinTransactionsRepository coinTransactionsService;

    @Autowired
    MembershipRepository membershipService;

    @Autowired
    UserVouchersRepository userVouchersService;

    @Autowired
    VouchersRepository vouchersService;

    @Autowired
    Helpers helpers;

    @Autowired
    Constants constants;

    @Autowired
    RatingHelperService ratingHelperService;

    @Autowired
    RatingsRepository ratingsService;


    public long calculateDuration(long t1, long t2) {
        ZonedDateTime zonedDateTime1 = java.time.ZonedDateTime
                .ofInstant(java.time.Instant.ofEpochMilli(t1), java.time.ZoneId.of("Asia/Kolkata"));
        ZonedDateTime zonedDateTime2 = java.time.ZonedDateTime
                .ofInstant(java.time.Instant.ofEpochMilli(t2), java.time.ZoneId.of("Asia/Kolkata"));
        return Math.abs(java.time.temporal.ChronoUnit.MINUTES.between(zonedDateTime1.toLocalTime(), zonedDateTime2.toLocalTime()));
    }

    @SuppressWarnings("deprecation")
    @ApiOperation(value = "To create an event (can be a single or a recurring event)")
    @PostMapping("/create")
    public void createEvent(@RequestBody JSONObject event) throws IOException {
        Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
        ZonedDateTime zonedDateTime = java.time.ZonedDateTime
                .ofInstant(instance, java.time.ZoneId.of("Asia/Kolkata"));
        ObjectMapper objectMapper = new ObjectMapper();
        Event ev = objectMapper.readValue(event.toJSONString(), Event.class);
        ev.setId(UUID.randomUUID().toString());
        ev.setParticipantList(new ArrayList<String>());
        ev.setCostType((!StringUtils.isEmpty(event.getString("costType")) && event.getString("costType").equals("paid")) ? "paid" : "free");
        ev.setType(StringUtils.isEmpty(event.getString("type")) ? "session" : event.getString("type"));
        if (event.getString("eventName").toLowerCase().contains("tambola")) {
            List<Integer> numberCaller = new ArrayList<>();
            Map<String, Integer> liveTambola = new HashMap<>();
            for (int i = 1; i <= 90; i++) {
                numberCaller.add(i);
            }
            Collections.shuffle(numberCaller);
            liveTambola.put("index", -1);
            liveTambola.put("value", null);
            liveTambola.put("lastNumber", null);
            ev.setTambolaNumberCaller(numberCaller);
            ev.setLiveTambola(liveTambola);
        }
        if (!StringUtils.isEmpty(ev.getCron())) {
            TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
            CronSequenceGenerator generator = new CronSequenceGenerator(ev.getCron(), tz);
            Date nextExecutionDate = generator.next(Date.from(zonedDateTime.now().toInstant()));
            ev.setIsParent(true);
            ev.setEventDate("");
            eventService.save(ev);
            long duration = calculateDuration(Long.parseLong(ev.getEndTime()), nextExecutionDate.getTime());
            //long duration = (Long.parseLong(ev.getEndTime())- nextExecutionDate.getTime())/(60000);

            int i = 0;
            String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
//			Date d = new Date(ev.getStartTime());
            Event tempChild = null;
            while (i < Integer.parseInt(event.getString("occurance")) && nextExecutionDate != null) {
                Event childEvent = objectMapper.readValue(event.toJSONString(), Event.class);
                childEvent.setId(UUID.randomUUID().toString());
                childEvent.setIsParent(false);
                childEvent.setParentId(ev.getId());
                childEvent.setIsScheduled(false);
                childEvent.setCron("");
                childEvent.setStartTime("" + nextExecutionDate.getTime());
                childEvent.setEventDate("" + nextExecutionDate.getTime());
                long start = nextExecutionDate.getTime();
                System.out.println(duration);
                Date newEndTime = new Date(nextExecutionDate.getTime() + duration * 60000);
                childEvent.setEndTime("" + newEndTime.getTime());

                if (event.getString("eventName").toLowerCase().contains("tambola")) {
                    List<Integer> numberCaller = new ArrayList<>();
                    Map<String, Integer> liveTambola = new HashMap<>();
                    for (int k = 1; k <= 90; k++) {
                        numberCaller.add(k);
                    }
                    Collections.shuffle(numberCaller);
                    liveTambola.put("index", -1);
                    liveTambola.put("value", null);
                    liveTambola.put("lastNumber", null);
                    childEvent.setTambolaNumberCaller(numberCaller);
                    childEvent.setLiveTambola(liveTambola);
                }

                ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
                obj.setTopic(ev.getEventName());
                obj.setTimezone("Asia/Kolkata");
                obj.setDuration((int) duration);
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
                childEvent.setMeetingId("" + meetingId);
                eventService.save(childEvent);
                tempChild = childEvent;
                nextExecutionDate = generator.next(nextExecutionDate);
                i++;
            }
        } else {
            long duration = (Long.parseLong(ev.getEndTime()) - Long.parseLong(ev.getStartTime())) / (60000);
            ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
            obj.setTopic(ev.getEventName());
            obj.setTimezone("Asia/Kolkata");
            obj.setDuration((int) duration);
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
            ev.setMeetingId("" + meetingId);
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

    @ApiOperation(value = "to give user's their cashback of sessions")
    @PostMapping("/giveReward")
    public void giveReward(@RequestBody JSONObject params) throws ExecutionException, InterruptedException {
        JSONObject getRecentTransactionsParams = new JSONObject();
        getRecentTransactionsParams.put("phone", params.getString("phone"));
        UserMemberships userMembership = membershipController.getMembershipByPhone(getRecentTransactionsParams);
        CollectionReference coinTransactionsRef = coinTransactionsService.getCollectionReference();
        Query cashbackQuery = coinTransactionsRef.whereEqualTo("phone", params.getString("phone")).whereEqualTo("source", "coinback").whereEqualTo("sourceId", params.getString("eventId"));
        ApiFuture<QuerySnapshot> newQuerySnapshot = cashbackQuery.get();
        CoinTransactions newTransaction = null;
        for (DocumentSnapshot document : newQuerySnapshot.get().getDocuments()) {
            newTransaction = document.toObject(CoinTransactions.class);
            break;
        }
        if (newTransaction != null) {
            return;
        }
        JSONObject eventParams = new JSONObject();
        eventParams.put("id", params.getString("eventId"));
        JSONObject eventObject = getEventById(eventParams);
        Event event = eventObject.getObject("event", Event.class);

        MembershipEnum membershipType = userMembership.getMembershipType();
        CollectionReference membershipRef = membershipService.getCollectionReference();

        Query membershipQuery = membershipRef.whereEqualTo("membershipType", membershipType);
        ApiFuture<QuerySnapshot> querySnapshot = membershipQuery.get();
        Membership membership = null;
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            membership = document.toObject(Membership.class);
            break;
        }

        int originalCoins = (int) Math.round(event.getCost() * membership.getRewardMultiplier());
        Random random = new Random();
        double percentage = -0.05 + (0.1 * random.nextDouble());
        if (percentage > 0) {
            percentage *= 0.5;
        }
        int coinsToGive = (int) Math.round(originalCoins * (1 + percentage));
        coinsToGive = Math.max(coinsToGive, 1);

        CoinTransactions transaction = new CoinTransactions();
        transaction.setAmount(coinsToGive);
        transaction.setId(UUID.randomUUID().toString());
        transaction.setPhone(params.getString("phone"));
        transaction.setSource("coinback");
        transaction.setSourceId(params.getString("eventId"));
        transaction.setTitle("Coinback for " + event.getEventName());
        transaction.setType(TransactionTypeEnum.CREDIT);
        transaction.setScratched(false);

        // don't add coins yet, will add after scratching of scratch card
//        userMembership.setCoins(userMembership.getCoins() + (int)coinsToGive);

        coinTransactionsService.save(transaction);
    }

    @ApiOperation(value = "Get events by date range (used when user clicks a date on the app)")
    @PostMapping("getEventsByDate")
    public JSONObject getEventsByDate(@RequestBody JSONObject params) {
        Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
        ZonedDateTime zonedDateTime = java.time.ZonedDateTime
                .ofInstant(instance, java.time.ZoneId.of("Asia/Kolkata"));
        zonedDateTime = zonedDateTime.with(LocalTime.of(23, 59));
        CollectionReference eventsRef = eventService.getCollectionReference();
        Query queryNew = eventsRef.whereEqualTo("isParent", false);

        if (params.getString("midnightDate") != null) {
            Filter filter = Filter.and(
                    Filter.lessThanOrEqualTo("startTime", params.getString("midnightDate")),
                    Filter.greaterThanOrEqualTo("startTime", params.getString("date"))
            );

            queryNew = queryNew.where(filter);
        } else {
            queryNew = queryNew.whereGreaterThanOrEqualTo("startTime",params.getString("date"));
            queryNew = queryNew.whereLessThanOrEqualTo("endTime", "" + zonedDateTime.toInstant().toEpochMilli());
        }
        ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

        Set<Event> eventsNew = new HashSet<>();
        Map<String, Double> subCategoryRatings = new HashMap<>();
        try {
            for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
                Event newEvent = document.toObject(Event.class);
                eventsNew.add(newEvent);
            }
            Set<String> uniqueSubCategories = new HashSet<>();
            for (Event event : eventsNew) {
                if (event.getSubCategory() == null) continue;
                uniqueSubCategories.add(event.getSubCategory());
            }
            for (String subCategory : uniqueSubCategories) {
                double rating = ratingHelperService.getRatingByCategory(subCategory);
                subCategoryRatings.put(subCategory, rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Event> eventsNewBest = IterableUtils.toList(eventsNew);

        Collections.sort(eventsNewBest, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        JSONObject output = new JSONObject();
        output.put("events", eventsNewBest);
        output.put("ratings", subCategoryRatings);
        return output;
    }

    @ApiOperation(value = "Save user's rating for a session/workshop")
    @PostMapping("/submitRating")
    public void submitRating(@RequestBody JSONObject params) {
        String phone = params.getString("phone");
        String reason = params.containsKey("reason") ? params.getString("reason") : null;
        String usersRating = params.getString("rating");
        String subCategory = params.getString("subCategory");
        String eventId = params.getString("id");
        Ratings rating = new Ratings();
        rating.setId(UUID.randomUUID().toString());
        rating.setEventId(eventId);
        rating.setPhone(phone);
        rating.setRating(Integer.parseInt(usersRating));
        rating.setSubCategory(subCategory);
        if (reason != null) {
            rating.setReason(reason);
        }
        ratingsService.save(rating);
    }

    @ApiOperation(value = "Get events by within date range")
    @PostMapping("getEventsWithinDateRange")
    public List<Event> getEventsWithinDateRange(@RequestBody JSONObject params) {
        CollectionReference eventsRef = eventService.getCollectionReference();
        Query queryNew = eventsRef.whereGreaterThanOrEqualTo("startTime", params.getString("minDate")).whereEqualTo("isParent", false);
        queryNew = queryNew.whereLessThanOrEqualTo("startTime", params.getString("maxDate"));

        ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

        Set<Event> eventsNew = new HashSet<>();
        try {
            for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
                eventsNew.add(document.toObject(Event.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Event> eventsNewBest = IterableUtils.toList(eventsNew);

        Collections.sort(eventsNewBest, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        return eventsNewBest;
    }

    @ApiOperation(value = "Get event by ID")
    @PostMapping("getEvent")
    public JSONObject getEventById(@RequestBody JSONObject params) {
        String id = params.getString("id");

        CollectionReference eventsRef = eventService.getCollectionReference();
        Query query = eventsRef.whereEqualTo("id", id);

        ApiFuture<QuerySnapshot> querySnapshotNew = query.get();

        Event event = null;
        try {
            for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
                event = document.toObject(Event.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject output = new JSONObject();
        output.put("event", event);
        return output;
    }

    @ApiOperation(value = "To book an event")
    @PostMapping("bookEvent")
    public String bookEvent(@RequestBody JSONObject params) throws IOException, MessagingException, GeneralSecurityException, InterruptedException, ExecutionException {
        CollectionReference eventRef = eventService.getCollectionReference();
        CollectionReference referrals = referralService.getCollectionReference();

        JSONObject getMembershipByPhoneParams = new JSONObject();
        getMembershipByPhoneParams.put("phone", params.getString("phoneNumber"));
        UserMemberships userMembership = membershipController.getMembershipByPhone(getMembershipByPhoneParams);
        CoinTransactions newTransaction = new CoinTransactions();

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
        event.setSeatsLeft(event.getSeatsLeft() - 1);
        List<String> participants = event.getParticipantList();
        if (participants == null) {
            participants = new ArrayList<String>();
        }
        participants.add(params.getString("phoneNumber"));

        event.setParticipantList(participants);

        // deduct coins from user's wallet if paid event
        double resultantCost = event.getCost();
        UserVouchers userVoucher = null;
        if (params.getString("voucherId") != null) {
            CollectionReference userVouchersRef = userVouchersService.getCollectionReference();
            CollectionReference vouchersRef = vouchersService.getCollectionReference();
            Query userVoucherQuery = userVouchersRef.whereEqualTo("voucherId", params.getString("voucherId"));
            Query voucherQuery = vouchersRef.whereEqualTo("id", params.getString("voucherId"));
            ApiFuture<QuerySnapshot> querySnapshotVoucher = voucherQuery.get();
            ApiFuture<QuerySnapshot> querySnapshotUserVoucher = userVoucherQuery.get();
            Vouchers voucher = null;
            for (DocumentSnapshot document : querySnapshotVoucher.get().getDocuments()) {
                voucher = document.toObject(Vouchers.class);
                break;
            }
            if (voucher.getStatus() != VoucherStatusEnum.ACTIVE) {
                return "FAILED";
            }
            for (DocumentSnapshot document : querySnapshotUserVoucher.get().getDocuments()) {
                userVoucher = document.toObject(UserVouchers.class);
                break;
            }
            Double value = voucher.getValue();
            Double limit = voucher.getLimit();
            Integer percent = voucher.getPercent();
            if (value != null && userVoucher.getStatus() == VoucherStatusEnum.ACTIVE) {
                resultantCost -= value;
            } else if (percent != null && userVoucher.getStatus() == VoucherStatusEnum.ACTIVE) {
                Double discount = (double) ((event.getCost() * percent) / 100);
                resultantCost -= limit != null ? Math.min(discount, limit) : discount;
            }
            userVoucher.setStatus(VoucherStatusEnum.REDEEMED);
            userVoucher.setRedemptionTime(new Date().getTime());
        }
        if (StringUtils.equals(event.getCostType(), "paid") && !userMembership.isFreeTrialActive()) {
            userMembership.setCoins((int) (userMembership.getCoins() - resultantCost));
            // add the data in user's transaction history
            newTransaction.setAmount((int) resultantCost);
            newTransaction.setSource(event.getType());
            newTransaction.setType(TransactionTypeEnum.DEBIT);
            newTransaction.setTitle("Book " + event.getEventName() + " session");
            newTransaction.setSourceId(event.getId());
            newTransaction.setTransactionDate(new Date().getTime());
            newTransaction.setPhone(params.getString("phoneNumber"));
            newTransaction.setId(UUID.randomUUID().toString());
        }

        if (userVoucher != null)
            userVouchersService.save(userVoucher);
        userMembershipsService.save(userMembership);
        if (StringUtils.equals(event.getCostType(), "paid") && !userMembership.isFreeTrialActive())
            coinTransactionsService.save(newTransaction);

        //TAMBOLA GENERATION-START
        List<String> tambolaTickets = event.getTambolaTickets();
        if (tambolaTickets == null) {
            tambolaTickets = new ArrayList<String>();
        }
        if (event.getEventName().toLowerCase().contains("tambola")) {
            tambolaTickets.add(ticket);
        }

        event.setTambolaTickets(tambolaTickets);
        //TAMBOLA GENERATION-END

        Map<String, Object> map = new HashMap<>();
        map.put("participantList", participants);
        map.put("seatsLeft", event.getSeatsLeft());
        map.put("tambolaTickets", tambolaTickets);
        eventRef.document(params.getString("id")).update(map);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(event.getStartTime()));
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        String currentContent = constants.getEventContent();
        currentContent = currentContent.replace("${title}", event.getEventName());
        currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
        currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
        currentContent = currentContent.replace("${meetingId}", event.getMeetingId() != null ? event.getMeetingId() : "");
        //content = content.replace("${zoomLink}", event.getMeetingLink());
        currentContent = currentContent.replace("${date}", FOMATTER.format(((GregorianCalendar) calendar).toZonedDateTime()));
        UserProfile user = userProfileController.getUserByPhone(params).getObject("user", UserProfile.class);
        if (user != null) {
            currentContent = currentContent.replace("${name}", user.getName());
            currentContent = currentContent.replace("${name}", user.getName());
            Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
            sessionsAttended++;
            user.setSessionsAttended("" + sessionsAttended);
            userProfileService.save(user);
            if (!StringUtils.isEmpty(user.getEmail()))
                emailService.sendSimpleMessage(user.getEmail(), event.getEventName(), currentContent);
        }

        return "SUCCESS";
    }

    public String sendEmail(String email, String subject, String content) {
        try {
            emailService.sendSimpleMessage(email, subject, content);
        } catch (MessagingException | IOException | GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "SUCCESS";
    }

    @ApiOperation(value = "To cancel an event")
    @PostMapping("cancelEvent")
    public String cancelEvent(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException {
        CollectionReference eventRef = eventService.getCollectionReference();

        JSONObject getMembershipByPhoneParams = new JSONObject();
        getMembershipByPhoneParams.put("phone", params.getString("phoneNumber"));
        UserMemberships userMembership = membershipController.getMembershipByPhone(getMembershipByPhoneParams);
        Optional<Event> oevent = eventService.findById(params.getString("id"));
        Event event = oevent.orElse(null);
		if (event == null) {
			return "FAILED: EVENT NOT FOUND";
		}

        event.setSeatsLeft(event.getSeatsLeft() + 1);
        List<String> participants = event.getParticipantList();
        int index = participants.indexOf(params.getString("phoneNumber"));
        participants.set(index, null);
        event.setParticipantList(participants);

        // refund coins to user's wallet in case of paid event
        CoinTransactions newTransaction = new CoinTransactions();
        if (StringUtils.equals(event.getCostType(), "paid") && !userMembership.isFreeTrialActive()) {
            userMembership.setCoins(userMembership.getCoins() + event.getCost());

            // add the data in user's transaction history
            newTransaction.setAmount(event.getCost());
            newTransaction.setSource("refund");
            newTransaction.setSourceId(event.getId());
            newTransaction.setTitle("Refund of " + event.getEventName() + " session");
            newTransaction.setType(TransactionTypeEnum.CREDIT);
            newTransaction.setId(UUID.randomUUID().toString());
            newTransaction.setTransactionDate(new Date().getTime());
            newTransaction.setPhone(params.getString("phoneNumber"));
        }

        userMembershipsService.save(userMembership);
        if (StringUtils.equals(event.getCostType(), "paid") && !userMembership.isFreeTrialActive())
            coinTransactionsService.save(newTransaction);
        Map<String, Object> map = new HashMap<>();
        if (event.getEventName().toLowerCase().contains("tambola")) {
            List<String> tickets = event.getTambolaTickets();
            if (tickets != null) {
//				tickets.remove(index);
                tickets.set(index, null);
                event.setTambolaTickets(tickets);
                map.put("tambolaTickets", tickets);
            }
        }
        map.put("participantList", participants);
        map.put("seatsLeft", event.getSeatsLeft());
        eventRef.document(params.getString("id")).update(map);
        try {
            JSONObject userJson = userProfileController.getUserByPhone(params);
            UserProfile user = userJson.getObject("user", UserProfile.class);
            Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
            sessionsAttended--;
            user.setSessionsAttended("" + sessionsAttended);
            userProfileService.save(user);
        } catch (IOException | InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "SUCCESS";
    }

    @ApiOperation(value = "Get list of past sessions attended by a user")
    @PostMapping("mySessions")
    public JSONObject mySessions(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException {
        Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
        ZonedDateTime zonedDateTime = java.time.ZonedDateTime
                .ofInstant(instance, java.time.ZoneId.of("Asia/Kolkata"));
        CollectionReference eventsRef = eventService.getCollectionReference();
        JSONObject getMembershipByPhoneParams = new JSONObject();
        getMembershipByPhoneParams.put("phone", params.getString("phoneNumber"));
        UserMemberships userMembership = membershipController.getMembershipByPhone(getMembershipByPhoneParams);
        Query query2 = eventsRef.whereLessThan("endTime", "" + zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("phoneNumber")).whereEqualTo("isParent", false);
        JSONObject emptyEventsObject = new JSONObject();
        List<Event> list = new ArrayList<>();
        emptyEventsObject.put("expiredEvents", list);
        if (userMembership == null) return emptyEventsObject;
        if (userMembership.getMembershipType() == MembershipEnum.Free) return emptyEventsObject;
        else if (userMembership.getMembershipType() == MembershipEnum.Silver) {
            long currTime = new Date().getTime();
            long twelveDaysAgo = currTime - 12 * 24 * 60 * 60 * 1000;
            query2 = query2.whereGreaterThanOrEqualTo("startTime", "" + twelveDaysAgo);
        } else if (userMembership.getMembershipType() == MembershipEnum.Gold) {
            long currTime = new Date().getTime();
            long thirtyDaysAgo = currTime - 30L * 24 * 60 * 60 * 1000;
            query2 = query2.whereGreaterThanOrEqualTo("startTime", "" + thirtyDaysAgo);
        }
        ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();

        Set<Event> events2 = new HashSet<>();
        try {
            for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
                events2.add(document.toObject(Event.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject output = new JSONObject();
        ArrayList<Event> events2List = new ArrayList<>(events2);
        events2List.sort(Comparator.comparing(Event::getStartTime));
        Collections.reverse(events2List);
        output.put("expiredEvents", events2List);

        return output;
    }

    public JSONObject getOngoingEvents(JSONObject params) {
        System.out.println(params.getString("date"));

        Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
        ZonedDateTime zonedDateTime = java.time.ZonedDateTime
                .ofInstant(instance, java.time.ZoneId.of("Asia/Kolkata"));
        zonedDateTime = zonedDateTime.with(LocalTime.of(23, 59));
        System.out.println(zonedDateTime);

        System.out.println(zonedDateTime.toInstant().toEpochMilli());
        CollectionReference eventsRef = eventService.getCollectionReference();
        Long newStartTime = Long.parseLong(params.getString("date")) + 600000;
        Long newEndTime = Long.parseLong(params.getString("endDate")) + 600000;

        Query query1 = eventsRef.whereLessThan("startTime", "" + newStartTime);
        Query query2 = eventsRef.whereGreaterThan("endTime", "" + newEndTime);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        events1.retainAll(events2);
        events1.retainAll(events3);
        List<Event> events = IterableUtils.toList(events1);
        Collections.sort(events, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
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
            if (membership.isFreeTrialActive()) {
                return "SUCCESS";
            } else if (membership.getMembershipType() == MembershipEnum.Free) {
                return "FAILED:NON-MEMBER";
            } else if (event.getCost() > membership.getCoins()) {
                return "FAILED:COST";
            }
        }
        return "SUCCESS";
    }
}  

