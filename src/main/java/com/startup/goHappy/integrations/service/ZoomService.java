package com.startup.goHappy.integrations.service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

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
	@Value("${zoom.password}")
    private String yourPass;
	@Value("${zoom.apiKey}")
    private String zoomApiKey;
	@Value("${zoom.apiSecret}")
    private String zoomApiSecret;

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
       // replace zoomUserId with your user ID
//		String accountId = "7002431600";
//		String zoomUserId = "kathuria.soham96@gmail.com";
//		String yourPass = "Always@2022@kind";
        String apiUrl = "https://api.zoom.us/v2/users/"+zoomUserId+"/meetings";
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
        headers.add("Authorization", "Bearer "+generateZoomJWTTOken());
        headers.add("content-type", "application/json");
        HttpEntity<ZoomMeetingObjectDTO> httpEntity = new HttpEntity<ZoomMeetingObjectDTO>(zoomMeetingObjectDTO, headers);
        ResponseEntity<ZoomMeetingObjectDTO> zEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity, ZoomMeetingObjectDTO.class);
        
//        HttpEntity<String> entity = new HttpEntity<String>(headers);
//        ResponseEntity<ZoomMeetingObjectDTO> zEntity =restTemplate.exchange(apiUrl2,HttpMethod.GET,entity,ZoomMeetingObjectDTO.class);
        
       
        if(zEntity.getStatusCodeValue() == 201) {
            return zEntity.getBody();
        } else {
        }
        return zoomMeetingObjectDTO;
    }
	
    public ZoomMeetingObjectDTO getZoomMeetingById(String meetingId) {
        String getMeetingUrl = "https://api.zoom.us/v2/meetings/" + meetingId;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateZoomJWTTOken());
        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<ZoomMeetingObjectDTO> zoomEntityRes =  restTemplate
            .exchange(getMeetingUrl, HttpMethod.GET, requestEntity, ZoomMeetingObjectDTO.class);
        if(zoomEntityRes.getStatusCodeValue() == 200) {
                return zoomEntityRes.getBody();
        } else if (zoomEntityRes.getStatusCodeValue() == 404) {
        }
        return null;
    }
    
    public String getRecordingById(String meetingId) {
        String getMeetingUrl = "https://api.zoom.us/v2/meetings/" + meetingId + "/recordings";
//        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + generateZoomJWTTOken());
        headers.add("content-type", "application/json");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<JSONObject> zoomEntityRes =  restTemplate
            .exchange(getMeetingUrl, HttpMethod.GET, requestEntity, JSONObject.class);
        if(zoomEntityRes.getStatusCodeValue() == 200) {
            return zoomEntityRes.getBody().getString("share_url");
        } else if (zoomEntityRes.getStatusCodeValue() == 404) {
        }
        return null;
    }
	
	private String generateZoomJWTTOken() {
		
//		String zoomApiKey = "ixMflExhTP2akOqgsFQ9eg";
//        String zoomApiSecret = "JGh6f5RX1SQfsw8iabdT0l6RUcFnj2MVAHUi";
        String id = UUID.randomUUID().toString().replace("-", "");
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        Date creation = new Date(System.currentTimeMillis());

        Date tokenExpiry = new Date(System.currentTimeMillis() + (1000 * 60 * 60));

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(zoomApiSecret);
        Key signingKey = new SecretKeySpec(zoomApiSecret.getBytes(),signatureAlgorithm.getJcaName());
        return Jwts.builder()
                .setId(id)
                .setIssuer(zoomApiKey)
                .setIssuedAt(creation)
                .setSubject("")
                .setExpiration(tokenExpiry)
                .signWith(signatureAlgorithm, signingKey)
                .compact();
    }

}
