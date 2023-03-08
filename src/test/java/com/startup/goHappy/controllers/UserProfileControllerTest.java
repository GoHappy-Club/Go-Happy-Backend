package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.UsesSunMisc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserProfileControllerTest {
    private MockMvc mockMvc;
    @Mock
    UserProfileRepository userProfileService;

    @Mock
    ResponseEntity<JSONObject> responseEntity;
    @Mock
    JSONObject jsonObject;

    @BeforeEach //instead of @Before have to use @BeforeEach in junit5
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserProfileController(userProfileService)).build();
    }

    private List<UserProfile> mockUserProfile() {
        List<UserProfile> userProfiles = new ArrayList<>();
        UserProfile userProfile = new UserProfile();
        userProfile.setId("user_1");
        userProfile.setPhone("123456789");
        userProfile.setAge("20");
        userProfile.setEmail("user_1@gmail.com");
        userProfiles.add(userProfile);
        return userProfiles;
    }


    @Test
    public void findAllShouldReturnResults() throws Exception {
        //when(userProfileService.retrieveAll()).thenReturn(mockUserProfile());
        ResultActions result = this.mockMvc.perform(post("/user/findAll"))
                .andExpect(status().isOk());
    }
}
