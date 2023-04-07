package com.startup.goHappy.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import com.startup.goHappy.controllers.AuthController;
import com.startup.goHappy.controllers.UserProfileController;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
//@WebMvcTest(value = AuthControllerTest.class)
public class AuthControllerTest {

    @Autowired
    private AuthController authController;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @MockBean
    private UserProfileController userProfileController;


    @Mock
    private CollectionReference userProfiles;

    @Mock
    private Query query;

    @Mock
    private ApiFuture<QuerySnapshot> querySnapshot;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private ApiFuture<Void> voidApiFuture;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegister() throws Exception {
        // Create user profile and referral JSON objects
        JSONObject userProfile = new JSONObject();
        userProfile.put("phone", "1234567890");
        JSONObject referDetails = new JSONObject();
        referDetails.put("referralId", "ABCD1234");

        // Mock UserProfileRepository's getCollectionReference() method
        CollectionReference collectionReference = Mockito.mock(CollectionReference.class);
        Mockito.when(userProfileRepository.getCollectionReference()).thenReturn(collectionReference);

        // Mock Query and DocumentSnapshot
        Query query = Mockito.mock(Query.class);
        Mockito.when(collectionReference.whereEqualTo(Mockito.anyString(), Mockito.anyString())).thenReturn(query);
        QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
        Mockito.when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        DocumentSnapshot documentSnapshot = Mockito.mock(DocumentSnapshot.class);
        Mockito.when(querySnapshot.getDocuments()).thenReturn((List) Collections.singletonList(documentSnapshot));

        // Mock UserProfile and UserProfileController
        UserProfile refereeUser = new UserProfile();
        refereeUser.setPhone("0987654321");
        Mockito.when(documentSnapshot.toObject(UserProfile.class)).thenReturn(refereeUser);
        Mockito.doNothing().when(userProfileController).create(userProfile);

        // Call register method and verify referral is created
        authController.register(userProfile, referDetails);
        Mockito.verify(userProfileController, Mockito.times(1)).create(userProfile);
        Mockito.verify(userProfileController, Mockito.times(1)).refer(Mockito.any(Referral.class));
    }

    @Test
    public void testLogin() throws IOException, InterruptedException, ExecutionException {
        JSONObject params = new JSONObject();
        params.put("phone", "1234567890");
        params.put("token", "abcd1234");
        params.put("referralId", "ref123");

        CollectionReference mockCollection = mock(CollectionReference.class);
        when(userProfileRepository.getCollectionReference()).thenReturn(mockCollection);

        Query mockQuery = mock(Query.class);
        when(mockCollection.whereEqualTo(any(String.class), any(String.class))).thenReturn(mockQuery);

        ApiFuture<QuerySnapshot> mockQuerySnapshotFuture = mock(ApiFuture.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
        UserProfile mockUserProfile = new UserProfile();
        when(mockDocumentSnapshot.toObject(UserProfile.class)).thenReturn(mockUserProfile);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mock(List.class));
        when(mockQuerySnapshot.getDocuments()).thenReturn((List) Arrays.asList(mockDocumentSnapshot));
        when(mockQuery.get()).thenReturn(mockQuerySnapshotFuture);
        when(mockQuerySnapshotFuture.get()).thenReturn(mockQuerySnapshot);

        UserProfile actualResult = authController.login(params);

        assertNotNull(actualResult);
        assertEquals(mockUserProfile, actualResult);
    }

    @Test
    public void testLoginWithToken() throws IOException, InterruptedException, ExecutionException {
        JSONObject params = new JSONObject();
        params.put("token", "abcd1234");
        params.put("referralId", "ref123");
        params.put("phone", "1234567890");

        CollectionReference mockCollection = mock(CollectionReference.class);
        when(userProfileRepository.getCollectionReference()).thenReturn(mockCollection);

        Query mockQuery = mock(Query.class);
        when(mockCollection.whereEqualTo(any(String.class), any(String.class))).thenReturn(mockQuery);

        ApiFuture<QuerySnapshot> mockQuerySnapshotFuture = mock(ApiFuture.class);
        QuerySnapshot mockQuerySnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);
        UserProfile mockUserProfile = new UserProfile();
        when(mockDocumentSnapshot.toObject(UserProfile.class)).thenReturn(mockUserProfile);
        when(mockQuerySnapshot.getDocuments()).thenReturn(mock(List.class));
        when(mockQuerySnapshot.getDocuments()).thenReturn((List) Arrays.asList(mockDocumentSnapshot));
        when(mockQuery.get()).thenReturn(mockQuerySnapshotFuture);
        when(mockQuerySnapshotFuture.get()).thenReturn(mockQuerySnapshot);

        UserProfile actualResult = authController.login(params);

        assertNotNull(actualResult);
        assertEquals(mockUserProfile, actualResult);
    }


}
