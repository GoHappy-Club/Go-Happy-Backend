package com.startup.goHappy.controllers;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.CollectionReference;
import com.startup.goHappy.config.Firestore.FirestoreConfig;
import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.PaymentLogRepository;
import com.startup.goHappy.entities.repository.ReferralRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {UserProfileController.class, FirestoreConfig.class})
@ExtendWith(SpringExtension.class)
class UserProfileControllerTest {
    @MockBean
    private PaymentLogRepository paymentLogRepository;

    @MockBean
    private ReferralRepository referralRepository;

    @Autowired
    private UserProfileController userProfileController;

    @MockBean
    private UserProfileRepository userProfileRepository;

    /**
     * Method under test: {@link UserProfileController#create(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testCreate() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R020 Temporary files were created but not deleted.
        //   The method under test created the following temporary files without deleting
        //   them:
        //     /Users/rakshitsharma/Codes/Go-Happy-Backend/libconscrypt_openjdk_jni-osx-x86_6416809071494610000.dylib
        //   Please ensure that temporary files are deleted in the method under test.
        //   See https://diff.blue/R020

        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.create(UserProfileController.java:77)
        //   In order to prevent create(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   create(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.create(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#create(JSONObject)}
     */
    @Test
    void testCreate2() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R020 Temporary files were created but not deleted.
        //   The method under test created the following temporary files without deleting
        //   them:
        //     /Users/rakshitsharma/Codes/Go-Happy-Backend/libconscrypt_openjdk_jni-osx-x86_6416809071494610000.dylib
        //   Please ensure that temporary files are deleted in the method under test.
        //   See https://diff.blue/R020

        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        when(userProfileRepository.save((UserProfile) any())).thenReturn(true);
        UserProfileController userProfileController = new UserProfileController(userProfileRepository);
        userProfileController.create(new JSONObject());
        verify(userProfileRepository).save((UserProfile) any());
    }

    /**
     * Method under test: {@link UserProfileController#create(JSONObject)}
     */
    @Test
    void testCreate3() {
        //   Diffblue Cover was unable to write a Spring test,
        //   so wrote a non-Spring test instead.
        //   Reason: R020 Temporary files were created but not deleted.
        //   The method under test created the following temporary files without deleting
        //   them:
        //     /Users/rakshitsharma/Codes/Go-Happy-Backend/libconscrypt_openjdk_jni-osx-x86_6416809071494610000.dylib
        //   Please ensure that temporary files are deleted in the method under test.
        //   See https://diff.blue/R020

        UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
        when(userProfileRepository.save((UserProfile) any())).thenReturn(true);
        UserProfileController userProfileController = new UserProfileController(userProfileRepository);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("age", "Value");
        userProfileController.create(jsonObject);
        verify(userProfileRepository).save((UserProfile) any());
    }

    /**
     * Method under test: {@link UserProfileController#getUserByEmail(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetUserByEmail() throws IOException, InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.getUserByEmail(UserProfileController.java:82)
        //   In order to prevent getUserByEmail(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   getUserByEmail(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.getUserByEmail(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#getUserByPhone(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testGetUserByPhone() throws IOException, InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.getUserByPhone(UserProfileController.java:100)
        //   In order to prevent getUserByPhone(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   getUserByPhone(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.getUserByPhone(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#setPaymentData(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testSetPaymentData() throws IOException, InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.setPaymentData(UserProfileController.java:119)
        //   In order to prevent setPaymentData(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   setPaymentData(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.setPaymentData(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#refer(Referral)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRefer() throws IOException, InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.refer(UserProfileController.java:195)
        //   In order to prevent refer(Referral)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   refer(Referral).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();

        Referral referral = new Referral();
        referral.setFrom("jane.doe@example.org");
        referral.setHasAttendedSession(true);
        referral.setId("42");
        referral.setReferralId("42");
        referral.setTime("Time");
        referral.setTo("alice.liddell@example.org");
        referral.setToName("To Name");
        referral.setToProfileImage("To Profile Image");
        userProfileController.refer(referral);
    }

    /**
     * Method under test: {@link UserProfileController#refer(Referral)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testRefer2() throws IOException, InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.refer(UserProfileController.java:195)
        //   In order to prevent refer(Referral)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   refer(Referral).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        Referral referral = mock(Referral.class);
        doNothing().when(referral).setFrom((String) any());
        doNothing().when(referral).setHasAttendedSession(anyBoolean());
        doNothing().when(referral).setId((String) any());
        doNothing().when(referral).setReferralId((String) any());
        doNothing().when(referral).setTime((String) any());
        doNothing().when(referral).setTo((String) any());
        doNothing().when(referral).setToName((String) any());
        doNothing().when(referral).setToProfileImage((String) any());
        referral.setFrom("jane.doe@example.org");
        referral.setHasAttendedSession(true);
        referral.setId("42");
        referral.setReferralId("42");
        referral.setTime("Time");
        referral.setTo("alice.liddell@example.org");
        referral.setToName("To Name");
        referral.setToProfileImage("To Profile Image");
        userProfileController.refer(referral);
    }

    /**
     * Method under test: {@link UserProfileController#tempApi()}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testTempApi() throws InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.tempApi(UserProfileController.java:206)
        //   In order to prevent tempApi()
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   tempApi().
        //   See https://diff.blue/R013 to resolve this issue.

        (new UserProfileController()).tempApi();
    }

    /**
     * Method under test: {@link UserProfileController#sessionAttended(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testSessionAttended() throws InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.sessionAttended(UserProfileController.java:227)
        //   In order to prevent sessionAttended(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   sessionAttended(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.sessionAttended(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#referralsList(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testReferralsList() throws InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.referralsList(UserProfileController.java:249)
        //   In order to prevent referralsList(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   referralsList(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.referralsList(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#topReferrals(JSONObject)}
     */
    @Test
    @Disabled("TODO: Complete this test")
    void testTopReferrals() throws InterruptedException, ExecutionException {
        // TODO: Complete this test.
        //   Reason: R013 No inputs found that don't throw a trivial exception.
        //   Diffblue Cover tried to run the arrange/act section, but the method under
        //   test threw
        //   java.lang.NullPointerException
        //       at com.startup.goHappy.controllers.UserProfileController.topReferrals(UserProfileController.java:301)
        //   In order to prevent topReferrals(JSONObject)
        //   from throwing NullPointerException, add constructors or factory
        //   methods that make it easier to construct fully initialized objects used in
        //   topReferrals(JSONObject).
        //   See https://diff.blue/R013 to resolve this issue.

        UserProfileController userProfileController = new UserProfileController();
        userProfileController.topReferrals(new JSONObject());
    }

    /**
     * Method under test: {@link UserProfileController#updateProfileImage(JSONObject)}
     */
    @Test
    void testUpdateProfileImage() throws Exception {
        when(userProfileRepository.getCollectionReference()).thenReturn(mock(CollectionReference.class));
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/user/updateProfileImage");
        postResult.characterEncoding("Encoding");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Key", "Value");
        String content = (new ObjectMapper()).writeValueAsString(jsonObject);
        MockHttpServletRequestBuilder requestBuilder = postResult.contentType(MediaType.APPLICATION_JSON).content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(userProfileController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(415));
    }

    /**
     * Method under test: {@link UserProfileController#updateUser(JSONObject)}
     */
    @Test
    void testUpdateUser() throws Exception {
        when(userProfileRepository.getCollectionReference()).thenReturn(mock(CollectionReference.class));
        MockHttpServletRequestBuilder postResult = MockMvcRequestBuilders.post("/user/update");
        postResult.characterEncoding("Encoding");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Key", "Value");
        String content = (new ObjectMapper()).writeValueAsString(jsonObject);
        MockHttpServletRequestBuilder requestBuilder = postResult.contentType(MediaType.APPLICATION_JSON).content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(userProfileController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(415));
    }
}

