package com.startup.goHappy.integrations.service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.alibaba.fastjson.JSON;
import com.startup.goHappy.integrations.model.ZoomParticipantsDTO;
import io.netty.handler.codec.base64.Base64Encoder;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.model.ZoomMeetingSettingsDTO;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;


@Service
public class ZoomService {

    @Value("${zoom.account}")
    private String zoomUserId;
    @Value("${zoom.accountId}")
    private String accountId;
    @Value("${zoom.clientId}")
    private String clientId;
    @Value("${zoom.clientSecret}")
    private String clientSecret;

    private RestTemplate restTemplate;

    public ZoomService() {
        this.restTemplate = new RestTemplate();
    }

    // used for testing
    @TestOnly
    public ZoomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ZoomMeetingObjectDTO createMeeting(ZoomMeetingObjectDTO zoomMeetingObjectDTO) {
        String apiUrl = "https://api.zoom.us/v2/users/" + zoomUserId + "/meetings";
        String apiUrl2 = "https://api.zoom.us/v2/meetings/6361126516";
        // replace with your password or method
//        zoomMeetingObjectDTO.setPassword();
        // replace email with your email
        zoomMeetingObjectDTO.setHost_email("kathuria.soham96@gmail.com");

        // Optional Settings for host and participant related options
        ZoomMeetingSettingsDTO settingsDTO = new ZoomMeetingSettingsDTO();
        settingsDTO.setJoin_before_host(false);
        settingsDTO.setParticipant_video(true);
        settingsDTO.setHost_video(false);
//        settingsDTO.setAuto_recording("cloud");
        settingsDTO.setMute_upon_entry(true);
        zoomMeetingObjectDTO.setSettings(settingsDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateZoomOAuth());
        headers.add("content-type", "application/json");
        HttpEntity<ZoomMeetingObjectDTO> httpEntity = new HttpEntity<ZoomMeetingObjectDTO>(zoomMeetingObjectDTO, headers);
        ResponseEntity<ZoomMeetingObjectDTO> zEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, ZoomMeetingObjectDTO.class);

//        HttpEntity<String> entity = new HttpEntity<String>(headers);
//        ResponseEntity<ZoomMeetingObjectDTO> zEntity =restTemplate.exchange(apiUrl2,HttpMethod.GET,entity,ZoomMeetingObjectDTO.class);


        if (zEntity.getStatusCodeValue() == 201) {
            return zEntity.getBody();
        } else {
        }
        return zoomMeetingObjectDTO;
    }

    public ZoomMeetingObjectDTO getZoomMeetingById(String meetingId) {
        String getMeetingUrl = "https://api.zoom.us/v2/meetings/" + meetingId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateZoomOAuth());
        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<ZoomMeetingObjectDTO> zoomEntityRes = restTemplate
                .exchange(getMeetingUrl, HttpMethod.GET, requestEntity, ZoomMeetingObjectDTO.class);
        if (zoomEntityRes.getStatusCodeValue() == 200) {
            return zoomEntityRes.getBody();
        } else if (zoomEntityRes.getStatusCodeValue() == 404) {
        }
        return null;
    }

    public String getRecordingById(String meetingId) {
        String getMeetingUrl = "https://api.zoom.us/v2/meetings/" + meetingId + "/recordings";
//        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        //headers.add("Authorization", "Bearer " + generateZoomOAuth());
        headers.add("Authorization", "Bearer ");
        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> zoomEntityRes = restTemplate
                .exchange(getMeetingUrl, HttpMethod.GET, requestEntity, JSONObject.class);
        if (zoomEntityRes.getStatusCodeValue() == 200) {
            return zoomEntityRes.getBody().getString("share_url");
        } else if (zoomEntityRes.getStatusCodeValue() == 404) {
        }
        return null;
    }

    public List<ZoomParticipantsDTO.Participant> getPastMeetingParticipants(String meetingId) {
        String getParticipantsUrl = "https://api.zoom.us/v2/report/meetings/" + meetingId + "/participants?include_fields=registrant_id&page_size=300";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateZoomOAuth());
        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<ZoomParticipantsDTO> zoomEntityRes = restTemplate
                .exchange(getParticipantsUrl, HttpMethod.GET, requestEntity, ZoomParticipantsDTO.class);
        if (zoomEntityRes.getStatusCodeValue() == 200) {
            return Objects.requireNonNull(zoomEntityRes.getBody()).getParticipants();
        }
        return null;
    }

    private String generateZoomOAuth() {
        String getTokenUrl = "https://zoom.us/oauth/token?grant_type=account_credentials&account_id=" + accountId;
//        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String auth = clientId + ":" + clientSecret;
        //headers.add("Authorization", "Bearer " + generateZoomOAuth());
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64(auth.getBytes())));

        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<JSONObject> oauthResult = restTemplate.exchange(getTokenUrl, HttpMethod.POST, requestEntity, JSONObject.class);
        if (oauthResult.getStatusCodeValue() == 200) {
            return oauthResult.getBody().getString("access_token");
        } else {
            System.out.println("Some Issue with Zoom Token API");
            return "Some Issue with Zoom Token API";
        }
    }
}
