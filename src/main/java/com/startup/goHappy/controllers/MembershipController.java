package com.startup.goHappy.controllers;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.startup.goHappy.entities.model.*;
import com.startup.goHappy.entities.repository.*;
import com.startup.goHappy.enums.MembershipEnum;
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.utils.Constants;
import com.startup.goHappy.utils.Helpers;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/membership")
public class MembershipController {
    @Autowired
    UserProfileRepository userProfileService;

    @Autowired
    UserProfileController userProfileController;

    @Autowired
    PaymentLogRepository paymentLogService;

    @Autowired
    MembershipRepository membershipService;

    @Autowired
    UserMembershipsRepository userMembershipsService;

    @Autowired
    CoinPackagesRepository coinPackagesService;

    @Autowired
    Constants constants;

    @Autowired
    Helpers helpers;

    @Autowired
    EmailService emailService;

    private final String subject = "GoHappy Club Family - Membership";

    @ApiOperation(value = "To fetch all memberships in subscription plan screen")
    @GetMapping("/listAll")
    public List<Membership> listAll() {
        Iterable<Membership> memberships = membershipService.retrieveAll();
        return IterableUtils.toList(memberships);
    }

    @ApiOperation(value = "To get all the coin pricing and packages")
    @GetMapping("/listCoinPackages")
    public List<CoinPackages> listCoinPackages() {
        Iterable<CoinPackages> packages = coinPackagesService.retrieveAll();
        return IterableUtils.toList(packages);
    }

    @ApiOperation(value = "When user completes payment for a subscription plan")
    @PostMapping("/buy")
    public void buySubscription(@RequestBody JSONObject params, @RequestParam String phoneNumber, @RequestParam String amount, @RequestParam String membershipId) throws ExecutionException, InterruptedException, IOException, MessagingException, GeneralSecurityException, FirebaseMessagingException {
        /*
        This API is callback for Phone Pe's successful/failed state update
        Expects :-
            phoneNumber
            amount
            membershipId
         */
        String encodedResponse = params.getString("response");

        byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse);
        String decodedString = new String(decodedBytes);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode decodedJson = objectMapper.readTree(decodedString);
        String code = decodedJson.get("code").asText();

        // if status is error, then return
        if ("PAYMENT_ERROR".equals(code)) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        CollectionReference userProfiles = userProfileService.getCollectionReference();
        CollectionReference membershipsRef = membershipService.getCollectionReference();
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();

        Query profileQuery = userProfiles.whereEqualTo("phone", phoneNumber);
        Query membershipQuery = membershipsRef.whereEqualTo("id", membershipId);
        Query userMembershipQuery = userMemberships.whereEqualTo("phone", phoneNumber);

        ApiFuture<QuerySnapshot> querySnapshot1 = profileQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot2 = membershipQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot3 = userMembershipQuery.get();

        Membership membership = null;
        for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
            membership = document.toObject(Membership.class);
            break;
        }

        UserMemberships userMembership = null;
        for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
            userMembership = document.toObject(UserMemberships.class);
            break;
        }

        UserProfile user = null;
        PaymentLog plog = new PaymentLog();
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            user = document.toObject(UserProfile.class);
            assert user != null;
            assert userMembership != null;
            assert membership != null;

            user.setLastPaymentAmount(membership.getSubscriptionFees());
            user.setLastPaymentDate("" + new Date().getTime());

            //set user's membership type and start/end date & append coins in user's wallet
            userMembership.setMembershipType(membership.getMembershipType());
            userMembership.setMembershipStartDate("" + calendar.getTimeInMillis());
            userMembership.setLastCoinsCreditedDate("" + calendar.getTimeInMillis());
            calendar.add(Calendar.MONTH, membership.getDuration());
            userMembership.setMembershipEndDate("" + calendar.getTimeInMillis());
            userMembership.setCoins(userMembership.getCoins() + membership.getCoinsPerMonth());

            // set user's phone and uid in this membership document
            userMembership.setUserId(user.getId());
            userMembership.setPhone(user.getPhone());

            plog.setPaymentDate(user.getLastPaymentDate());
            plog.setPhone(user.getPhone());
            plog.setId(UUID.randomUUID().toString());
            plog.setAmount(user.getLastPaymentAmount());
            plog.setType("membership");
            break;
        }

        userProfileService.save(user);
        userMembershipsService.save(userMembership);
        paymentLogService.save(plog);

        //send email to user
        String currentContent = constants.getJoiningContent();
        if (user != null) {
            currentContent = currentContent.replace("${name}", user.getName());
            currentContent = currentContent.replace("${name}", user.getName());
            if (!StringUtils.isEmpty(user.getEmail()))
                emailService.sendSimpleMessage(user.getEmail(), subject, currentContent);
        }

        // send data through fcm to the frontend for redux update
        String MEMBERSHIP_TOPIC = "subscriptionUpdate";
        sendSubscriptionUpdateWithFcm(user.getFcmToken(), MEMBERSHIP_TOPIC, userMembership);
    }

    @ApiOperation(value = "to cancel a user's subscription")
    @PostMapping("/cancel")
    public UserMemberships cancelSubscription(@RequestBody JSONObject params) throws ExecutionException, InterruptedException, MessagingException, GeneralSecurityException, IOException {
        /*
        Expects :-
            phoneNumber
            reason
         */
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();
        CollectionReference userProfiles = userProfileService.getCollectionReference();

        String phoneNumber = params.getString("phoneNumber");
        Query memberQuery = userMemberships.whereEqualTo("phone", phoneNumber);
        Query profileQuery = userProfiles.whereEqualTo("phone", phoneNumber);

        ApiFuture<QuerySnapshot> querySnapshot1 = memberQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot2 = profileQuery.get();

        // get the userMembership document corresponding to phoneNumber
        UserMemberships userMember = null;
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            userMember = document.toObject(UserMemberships.class);

            //set userMember's membership to free and dates to null
            assert userMember != null;
            userMember.setMembershipType(MembershipEnum.Free);
            userMember.setMembershipStartDate(null);
            userMember.setMembershipEndDate(null);
            userMember.setLastCoinsCreditedDate(null);
            userMember.setCancellationDate(helpers.FormatMilliseconds(new Date().getTime()));
            userMember.setCancellationReason(params.getString("reason"));
            break;
        }
        userMembershipsService.save(userMember);

        // get the user's profile(full)
        UserProfile user = null;
        for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
            user = document.toObject(UserProfile.class);
            break;
        }

        //send email to user
        String currentContent = constants.getCancelContent();
        if (user != null) {
            currentContent = currentContent.replace("${name}", user.getName());
            currentContent = currentContent.replace("${name}", user.getName());
            if (!StringUtils.isEmpty(user.getEmail()))
                emailService.sendSimpleMessage(user.getEmail(), subject, currentContent);
        }

        return userMember;
    }

    @ApiOperation(value = "to renew a user's subscription")
    @PostMapping("/renew")
    public void renewSubscription(@RequestBody JSONObject params, @RequestParam String phoneNumber, @RequestParam String amount, @RequestParam String membershipId) throws ExecutionException, InterruptedException, FirebaseMessagingException, JsonProcessingException {
        String encodedResponse = params.getString("response");

        byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse);
        String decodedString = new String(decodedBytes);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode decodedJson = objectMapper.readTree(decodedString);
        String code = decodedJson.get("code").asText();

        //check the status of the payment, if it is error, return
        if ("PAYMENT_ERROR".equals(code)) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        CollectionReference userProfiles = userProfileService.getCollectionReference();
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();
        CollectionReference membershipRef = membershipService.getCollectionReference();

        Query memberQuery = userMemberships.whereEqualTo("phone", phoneNumber);
        Query userQuery = userProfiles.whereEqualTo("phone", phoneNumber);
        Query membershipQuery = membershipRef.whereEqualTo("id", membershipId);

        ApiFuture<QuerySnapshot> querySnapshot1 = memberQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot2 = userQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot3 = membershipQuery.get();

        Membership membership = null;
        for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
            membership = document.toObject(Membership.class);
            break;
        }

        UserMemberships userMember = null;
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            userMember = document.toObject(UserMemberships.class);

            //change userMember's start/end date
            assert userMember != null;
            assert membership != null;
            userMember.setMembershipStartDate("" + calendar.getTimeInMillis());
            long timeDuration = Long.parseLong(userMember.getMembershipEndDate());
            calendar.setTimeInMillis(timeDuration);
            calendar.add(Calendar.MONTH, membership.getDuration());
            userMember.setMembershipEndDate("" + calendar.getTimeInMillis());
            break;
        }

        UserProfile user = null;
        for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
            user = document.toObject(UserProfile.class);
            user.setLastPaymentDate("" + new Date().getTime());
            user.setLastPaymentAmount(membership.getSubscriptionFees());

            PaymentLog plog = new PaymentLog();
            plog.setPaymentDate(user.getLastPaymentDate());
            plog.setPhone(user.getPhone());
            plog.setId(UUID.randomUUID().toString());
            plog.setAmount(user.getLastPaymentAmount());
            plog.setType("membership");
            paymentLogService.save(plog);
            break;
        }

        userMembershipsService.save(userMember);
        userProfileService.save(user);

        // send fcm notification to the frontend for redux update
        String MEMBERSHIP_TOPIC = "subscriptionUpdate";
        sendSubscriptionUpdateWithFcm(user.getFcmToken(), MEMBERSHIP_TOPIC, userMember);

    }

    @ApiOperation(value = "to renew a user's subscription")
    @PostMapping("/upgrade")
    public void upgradeSubscription(@RequestBody JSONObject params, @RequestParam String phoneNumber, @RequestParam String amount, @RequestParam String membershipId) throws ExecutionException, InterruptedException, FirebaseMessagingException, JsonProcessingException {

        /*
         * Expects
         *   phoneNumber
         *   membershipId
         *   amount
         * */

        String encodedResponse = params.getString("response");

        byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse);
        String decodedString = new String(decodedBytes);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode decodedJson = objectMapper.readTree(decodedString);
        String code = decodedJson.get("code").asText();

        //check the status of the payment, if it is error, return
        if ("PAYMENT_ERROR".equals(code)) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        CollectionReference userProfiles = userProfileService.getCollectionReference();
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();
        CollectionReference membershipRef = membershipService.getCollectionReference();

        Query memberQuery = userMemberships.whereEqualTo("phone", phoneNumber);
        Query userQuery = userProfiles.whereEqualTo("phone", phoneNumber);
        Query membershipQuery = membershipRef.whereEqualTo("id", membershipId);

        ApiFuture<QuerySnapshot> querySnapshot1 = memberQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot2 = userQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot3 = membershipQuery.get();

        Membership membership = null;
        for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
            membership = document.toObject(Membership.class);
            break;
        }

        UserMemberships userMember = null;
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            userMember = document.toObject(UserMemberships.class);

            //change userMember's membership type and Add more coins in wallet
            assert membership != null;
            assert userMember != null;
            userMember.setMembershipType(membership.getMembershipType());
            userMember.setCoins(userMember.getCoins() + membership.getCoinsPerMonth());
            // set start and end date
            userMember.setMembershipStartDate("" + calendar.getTimeInMillis());
            userMember.setLastCoinsCreditedDate("" + calendar.getTimeInMillis());
            calendar.add(Calendar.MONTH, membership.getDuration());

            userMember.setMembershipEndDate("" + calendar.getTimeInMillis());
            break;
        }

        UserProfile user = null;
        PaymentLog plog = new PaymentLog();
        for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
            user = document.toObject(UserProfile.class);
            user.setLastPaymentDate("" + new Date().getTime());
            user.setLastPaymentAmount(membership.getSubscriptionFees());

            plog.setPaymentDate(user.getLastPaymentDate());
            plog.setPhone(user.getPhone());
            plog.setId(UUID.randomUUID().toString());
            plog.setAmount(user.getLastPaymentAmount());
            plog.setType("membership");
            break;
        }

        userMembershipsService.save(userMember);
        userProfileService.save(user);
        paymentLogService.save(plog);

        // send fcm notification to the frontend for redux update
        String MEMBERSHIP_TOPIC = "subscriptionUpdate";
        sendSubscriptionUpdateWithFcm(user.getFcmToken(), MEMBERSHIP_TOPIC, userMember);

    }

    @ApiOperation(value = "to top-up a user's wallet")
    @PostMapping("/topUp")
    public void topUpWallet(@RequestBody JSONObject params, @RequestParam String phoneNumber, @RequestParam String amount, @RequestParam String coinsToGive) throws ExecutionException, InterruptedException, IOException, FirebaseMessagingException {
        String encodedResponse = params.getString("response");

        byte[] decodedBytes = Base64.getDecoder().decode(encodedResponse);
        String decodedString = new String(decodedBytes);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode decodedJson = objectMapper.readTree(decodedString);
        String code = decodedJson.get("code").asText();

        //check the status of the payment, if it is error, return
        if ("PAYMENT_ERROR".equals(code)) return;

        JSONObject getUserByPhoneParams = new JSONObject();
        getUserByPhoneParams.put("phoneNumber", phoneNumber);
        JSONObject result = userProfileController.getUserByPhone(getUserByPhoneParams);
        UserProfile user = result.getObject("user", UserProfile.class);
        user.setLastPaymentAmount(Integer.parseInt(amount) / 100);
        user.setLastPaymentDate("" + new Date().getTime());

        PaymentLog plog = new PaymentLog();
        plog.setId(UUID.randomUUID().toString());
        plog.setAmount(Integer.parseInt(amount) / 100);
        plog.setPhone(user.getPhone());
        plog.setType("topUp");
        plog.setPaymentDate("" + new Date().getTime());

        JSONObject getMembershipByPhoneParams = new JSONObject();
        getMembershipByPhoneParams.put("phone", phoneNumber);
        UserMemberships userMember = getMembershipByPhone(getMembershipByPhoneParams);

        if (userMember.getMembershipType() == MembershipEnum.Free) return;

        userMember.setCoins(userMember.getCoins() + Integer.parseInt(coinsToGive));

        userMembershipsService.save(userMember);
        userProfileService.save(user);
        paymentLogService.save(plog);

        // send fcm notification to the frontend for redux update
        String MEMBERSHIP_TOPIC = "subscriptionUpdate";
        sendSubscriptionUpdateWithFcm(user.getFcmToken(), MEMBERSHIP_TOPIC, userMember);

    }

    @ApiOperation(value = "to revoke a user's membership")
    @GetMapping("/expire")
    public UserMemberships expire(@RequestParam String phoneNumber) throws ExecutionException, InterruptedException, IOException, FirebaseMessagingException {
        JSONObject getMembershipByPhoneParams = new JSONObject();
        getMembershipByPhoneParams.put("phone", phoneNumber);
        UserMemberships userMembership = getMembershipByPhone(getMembershipByPhoneParams);
        userMembership.setMembershipType(MembershipEnum.Free);
        userMembership.setLastCoinsCreditedDate(null);
        userMembershipsService.save(userMembership);
        return userMembership;
    }

    @ApiOperation(value = "Get user's profile and membership document for update")
    @PostMapping("/getUserUpdates")
    public JSONObject getUserUpdates(@RequestBody JSONObject params) throws ExecutionException, InterruptedException {
        CollectionReference userProfiles = userProfileService.getCollectionReference();
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();

        Query userQuery = userProfiles.whereEqualTo("phone", params.getString("phoneNumber"));
        Query userMembershipQuery = userMemberships.whereEqualTo("phone", params.getString("phoneNumber"));
        ApiFuture<QuerySnapshot> querySnapshot1 = userQuery.get();
        ApiFuture<QuerySnapshot> querySnapshot2 = userMembershipQuery.get();

        UserProfile userProfile = null;
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            userProfile = document.toObject(UserProfile.class);
            break;
        }

        UserMemberships userMembership = null;
        for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
            userMembership = document.toObject(UserMemberships.class);
            break;
        }
        JSONObject userUpdates = new JSONObject();
        userUpdates.put("userProfile", userProfile);
        userUpdates.put("userMembership", userMembership);
        return userUpdates;
    }

    @ApiOperation(value = "Get membership data by phone")
    @PostMapping("/getMembershipByPhone")
    public UserMemberships getMembershipByPhone(@RequestBody JSONObject requestBody) throws ExecutionException, InterruptedException {

        CollectionReference userMemberships = userMembershipsService.getCollectionReference();

        Query userMembership = userMemberships.whereEqualTo("phone", requestBody.getString("phone"));
        ApiFuture<QuerySnapshot> querySnapshot1 = userMembership.get();

        UserMemberships userMemberships1 = null;
        for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
            userMemberships1 = document.toObject(UserMemberships.class);
            break;
        }

        return userMemberships1;
    }

    public UserMemberships createNewMembership(String phone, String userId) {
        UserMemberships userMembership = new UserMemberships();

        userMembership.setId(UUID.randomUUID().toString());
        userMembership.setUserId(userId);
        userMembership.setPhone(phone);

        userMembershipsService.save(userMembership);

        return userMembership;
    }

    private void sendSubscriptionUpdateWithFcm(String fcmToken, String topic, UserMemberships userMembership) throws FirebaseMessagingException {
        System.out.println("Recieved fcm token is ==>" + fcmToken);
        List<String> fcmTokens = new ArrayList<>();
        fcmTokens.add(fcmToken);

        try {
            FirebaseMessaging.getInstance().subscribeToTopic(fcmTokens, topic);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }

        try {
            Message message = Message.builder().setTopic(topic).putData("runUpdate", "true").putData("type", "subscriptionUpdate").putData("userMembership", JSONObject.toJSONString(userMembership)).build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent weekly contribution reminder: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Failed to send weekly contribution reminder: " + e.getMessage());
        }
        FirebaseMessaging.getInstance().unsubscribeFromTopic(fcmTokens, topic);
    }
}