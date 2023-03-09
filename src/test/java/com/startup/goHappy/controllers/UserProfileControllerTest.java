package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startup.goHappy.entities.model.UserProfile;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

    private Gson gson = new Gson();
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
//        user1.setDob("1/12/1999");
//        user1.setSessionsAttended("23");
//        user1.setDateOfJoining("1/2/2022");
//        user1.setSelfInviteCode("23");
//        user1.setMembership("ewsa");

        UserProfile user2 = new UserProfile();
        user2.setId("1235");
        user2.setAge("25");
        user2.setEmail("test1@gmail.com");

        users.add(user1);
        users.add(user2);

        JSONObject output = new JSONObject();
        output.put("users", users);

        Mockito.when(userProfileService.findAll()).thenReturn(output);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URI).accept(
                MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String outputStr = result.getResponse().getContentAsString();

        JsonObject jsonObject1 = gson.fromJson(String.valueOf(outputStr), JsonObject.class);

        jsonObject1 = sortJsonObjectByKeys(jsonObject1);


        JsonObject jsonObject = gson.fromJson(String.valueOf(outputStr), JsonObject.class);

        jsonObject1 = sortJsonObjectByKeys(jsonObject1);

        assertThat(String.valueOf(jsonObject1)).isEqualTo(String.valueOf(outputStr));
        assertEquals(result.getResponse().getStatus(), 200);

    }

//    @Test
//    public void getUserByEmailTest() throws Exception{
//        String URI = "/user/getUserByEmail";
//
//        UserProfile user1 = new UserProfile();
//        user1.setId("1234");
//        user1.setAge("23");
//        user1.setEmail("test@gmail.com");
//        user1.setName("abc");
//        user1.setPhone("123456789");
//
//        JSONObject input = new JSONObject();
//        input.put("email", "test@gmail.com");
//
//        System.out.println(input);
//        JSONObject output = new JSONObject();
//        output.put("user", user1);
//
//        Mockito.when(userProfileService.getUserByEmail(input)).thenReturn(output);
//
//        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URI).accept(
//                MediaType.APPLICATION_JSON);
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//
//        String outputStr = result.getResponse().getContentAsString();
//        System.out.println(outputStr);
//
//        JsonObject jsonObject1 = gson.fromJson(outputStr, JsonObject.class);
//
//        jsonObject1 = sortJsonObjectByKeys(jsonObject1);
//
//        assertThat(outputStr).isEqualTo(String.valueOf(jsonObject1));
//        assertEquals(result.getResponse().getStatus(), 200);
//    }
    private JsonObject sortJsonObjectByKeys(JsonObject jsonObject) {
        JsonObject sortedJsonObject = new JsonObject();
        jsonObject.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .forEach(e -> sortedJsonObject.add(e.getKey(), e.getValue()));
        return sortedJsonObject;
    }




}
