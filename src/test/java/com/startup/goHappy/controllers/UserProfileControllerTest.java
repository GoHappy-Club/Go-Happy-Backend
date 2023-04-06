package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startup.goHappy.entities.model.UserProfile;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
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
    static UserProfile user1 = new UserProfile();
    static UserProfile user2 = new UserProfile();

    static {
        user1.setId("1234");
        user1.setAge("23");
        user1.setEmail("test@gmail.com");
        user1.setName("abc");
        user1.setPhone("123456789");
        user1.setSessionsAttended("23");
        user1.setDateOfJoining("1/2/2022");
        user1.setSelfInviteCode("23");
        user1.setMembership("ewsa");
        user1.setProfileImage("bacsfds");
        user1.setSelfInviteCode("cnsdh");
        user1.setGoogleSignIn(true);
        user1.setPassword("adiufhnsdf");
        user1.setLastPaymentDate("2/2/2022");
        user1.setLastPaymentAmount(200);

        user2.setId("1235");
        user2.setAge("25");
        user2.setEmail("test1@gmail.com");
        user2.setName("Ibu");
        user2.setPhone("9876579874");
        user2.setSessionsAttended("9");
        user2.setDateOfJoining("11/2/2021");
        user2.setSelfInviteCode("xdjif3");
        user2.setMembership("sdkjv");
        user2.setProfileImage("ksdjf");
        user2.setSelfInviteCode("dkshfdio");
        user2.setGoogleSignIn(false);
        user2.setPassword("dfkjdin");
        user2.setLastPaymentDate("2/12/2022");
        user2.setLastPaymentAmount(500);
    }
    @Test
    public void testFindAll() throws Exception {

        String URI = "/user/findAll";

        List<UserProfile> users = new ArrayList<UserProfile>();
        users.add(user1);
        users.add(user2);

        JSONObject output = new JSONObject();
        output.put("users", users);

        Mockito.when(userProfileService.findAll()).thenReturn(output);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URI).accept(
                MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String data = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(data, String.valueOf(output), false);

        assertEquals(result.getResponse().getStatus(), 200);
    }
    @Test
    public void getUserByEmailTest() throws Exception{
        String URI = "/user/getUserByEmail";

        JSONObject input = new JSONObject();
        input.put("email", "test@gmail.com");

        JSONObject output = new JSONObject();
        output.put("user", user1);

        Mockito.when(userProfileService.getUserByEmail(input)).thenReturn(output);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(URI)
                .accept(MediaType.APPLICATION_JSON).content(String.valueOf(input))
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String outputStr = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(outputStr, String.valueOf(output), false);

        assertEquals(result.getResponse().getStatus(), 200);
    }
}
