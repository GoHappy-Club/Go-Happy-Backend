package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.auto.value.AutoAnnotation;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@WebMvcTest (value = UserProfileControllerTest.class)
//@SpringBootTest(classes = UserProfileControllerTest.class)
class UserProfileControllerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired MockMvc mockMvc;

    @MockBean UserProfileController userProfileService;


    @Test
    public void testFindAll() throws Exception {
        String URI = "/user/findAll";

        List<UserProfile> users = new ArrayList<UserProfile>();
        UserProfile user1 = new UserProfile();
        user1.setId("1234");
        user1.setAge("23");
        user1.setEmail("test@gmail.com");
        user1.setName("abc");
        user1.setPhone("123456789");
        user1.setDob("1/12/1999");
        user1.setSessionsAttended("23");
        user1.setDateOfJoining("1/2/2022");
        user1.setSelfInviteCode("23");
        user1.setMembership("ewsa");
        user1.set
        UserProfile user2 = new UserProfile();
        user2.setId("1235");
        user2.setAge("25");
        user2.setEmail("test1@gmail.com");

        users.add(user1);
        users.add(user2);

        JSONObject output = new JSONObject();
        output.put("users", users);
        System.out.println(output);

        Mockito.when(userProfileService.findAll()).thenReturn(output);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URI).accept(
                MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String outputStr = result.getResponse().getContentAsString();
        System.out.println("The output response is " + outputStr);
        assertThat(outputStr).isEqualTo(String.valueOf(output));
//        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

}