package com.startup.goHappy.notifications;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.Query;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.startup.goHappy.controllers.EventController;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.Fcm;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.FcmRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Service
public class CloudNotification {

    private final Firestore firestore;
    private final FirebaseApp firebaseApp;

    String SESSION_REMINDER_TOPIC = "session_reminders";

    String ALL_USERS_TOPIC = "all_users";


    @Autowired
    UserProfileRepository userProfileService;

    @Autowired
    FcmRepository fcmService;

    @Autowired
    EventController eventController;

    @Autowired
    public CloudNotification(Firestore firestore, FirebaseApp firebaseApp) {  // Inject both Firestore and FirebaseApp
        this.firestore = firestore;
        this.firebaseApp = firebaseApp;  // Assign to the field
    }


    // this one will run for users who registered for a session.
    // I need a cron job that keeps checking any new event in next 10 minutes. The cron can run ecery 2 minutes.
    // We can also leave it as of now, since this notification is already there in the app and uses react-native-notifications.
    @Scheduled(cron = "0 0/10 * * * *")
    public void sendSessionReminders() throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {

        JSONObject params = new JSONObject();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        long epochMillisIndia = calendar.getTimeInMillis();
        params.put("minDate",""+epochMillisIndia);
        long newTime = epochMillisIndia + 10 * 60 * 1000; //* 100 is additional
        params.put("maxDate",""+newTime);
        List<Event> events = eventController.getEventsWithinDateRange(params);
        for(Event event: events){
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date(Long.parseLong(event.getStartTime()));
            String formattedTime = dateFormat.format(date);
            System.out.println(formattedTime);
            List<String> phones = event.getParticipantList();
            String title = event.getEventName();
            String body = "Click below to attend the session at "+formattedTime;
            String image = event.getCoverImage();
            CollectionReference userProfiles = userProfileService.getCollectionReference();
            CollectionReference fcms = fcmService.getCollectionReference();
            List<String> fcmTokens = new ArrayList<>();
            for (int i = 0; i < phones.size(); i += 30) {
                List<String> phoneBatch = phones.subList(i, Math.min(i + 30, phones.size()));
                Query batchQuery = userProfiles.whereIn("phone", phoneBatch);
                ApiFuture<QuerySnapshot> querySnapshot = batchQuery.get();
                for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                    if(document.get("fcmToken")!=null) {
                        String fcm = document.get("fcmToken").toString();
                        fcmTokens.add(fcm);
                    }
                }
            }
            if(fcmTokens.isEmpty()){
                continue;
            }
            Query fcmQuery = fcms.whereEqualTo("topicName",SESSION_REMINDER_TOPIC);
            ApiFuture<QuerySnapshot> fcmQuerySnapshot = fcmQuery.get();
            Fcm existingFcm = null;
            for (DocumentSnapshot document : fcmQuerySnapshot.get().getDocuments()) {
                Fcm fcm = document.toObject(Fcm.class);
                existingFcm = fcm;
                if(fcm.getFcmTokens()!=null && fcm.getFcmTokens().size()>0) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(fcm.getFcmTokens(), SESSION_REMINDER_TOPIC);
                }
                break;
            }
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(fcmTokens,SESSION_REMINDER_TOPIC);
                existingFcm.setFcmTokens(fcmTokens);
                existingFcm.setTopicName(SESSION_REMINDER_TOPIC);
                fcmService.save(existingFcm);
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException(e);
            }
            try {
                Message message = Message.builder()
                        .setTopic(SESSION_REMINDER_TOPIC)
                        .putData("deepLink", "https://www.gohappyclub.in/session_details/"+event.getId())
//                        .putData("priority","HIGH")
                        .putData("confirmText", "Click here")
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setImage(image)
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println(response);
            } catch (FirebaseMessagingException e) {
                // Handle exception
            }
        }


    }

    //Ex. Khayal. This will be triggered manually or can think about it.
    public void promoteSession(String title, String body){

    }

    // This will run for people who never contributed and have a FcmToken. make it high priority notification
    public void contributionReminders(){

    }

//    Manually triggered when trip is added, we send a notification to all users and this will a high priority notification.
    public void sendTripUpdate(String title, String body, String image) throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {

        List<String> fcmTokens = userProfileService.retrieveAll()
                .stream()
                .filter(user -> user != null && user.getFcmToken() != null)
                .map(UserProfile::getFcmToken)
                .collect(Collectors.toList());

        CollectionReference fcms = fcmService.getCollectionReference();
        Query fcmQuery = fcms.whereEqualTo("topicName",ALL_USERS_TOPIC);

        ApiFuture<QuerySnapshot> fcmQuerySnapshot = fcmQuery.get();

        Fcm existingFcm = null;
        for (DocumentSnapshot document : fcmQuerySnapshot.get().getDocuments()) {
            Fcm fcm = document.toObject(Fcm.class);
            existingFcm = fcm;
            if(fcm.getFcmTokens()!=null && fcm.getFcmTokens().size()>0) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(fcm.getFcmTokens(), ALL_USERS_TOPIC);
            }
            break;
        }
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(fcmTokens,ALL_USERS_TOPIC);
            if(existingFcm==null){
                existingFcm = new Fcm();
                existingFcm.setId(UUID.randomUUID().toString());
            }
            existingFcm.setFcmTokens(fcmTokens);
            existingFcm.setTopicName(ALL_USERS_TOPIC);
            fcmService.save(existingFcm);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
        try {
            Message message = Message.builder()
                    .setTopic(ALL_USERS_TOPIC)
                    .putData("deepLink", "https://www.gohappyclub.in/refer")
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setImage(image)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println(response);
        } catch (FirebaseMessagingException e) {
            // Handle exception
        }

    }



}
