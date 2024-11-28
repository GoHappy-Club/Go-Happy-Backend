package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Festivals;
import com.startup.goHappy.entities.repository.FestivalsRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/festivals")
public class FestiveWishesController {
    @Autowired
    FestivalsRepository festivalsService;

    private RestTemplate restTemplate;

    @Value("${calendarific.apiKey}")
    private String apiKey;

    public FestiveWishesController() {
        this.restTemplate = new RestTemplate();
    }

    @ApiOperation(value = "Add festivals data for a particular year")
    @GetMapping("/add")
    public void addFestivals(@RequestParam String year) throws JsonProcessingException {
        String url = "https://calendarific.com/api/v2/holidays?api_key=" + apiKey + "&country=IN&year=" + year;
        String rawResponse = restTemplate.getForObject(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(rawResponse);

        JsonNode holidaysNode = rootNode.path("response").path("holidays");

        holidaysNode.forEach(holiday -> {
            Festivals festival = new Festivals();
            festival.setId(UUID.randomUUID().toString());
            festival.setName(holiday.path("name").asText());
            festival.setActive(false);
            festival.setIsoDate(holiday.path("date").path("iso").asText());
            festival.setTitle(holiday.path("description").asText());
            festivalsService.save(festival);
        });
    }

    @ApiOperation(value = "Update festivals data (asset or active)")
    @PostMapping("/update")
    public void addFestivals(@RequestBody JSONObject params) {
        ObjectMapper objectMapper = new ObjectMapper();
        Festivals festival = objectMapper.convertValue(params, Festivals.class);
        festivalsService.save(festival);
    }

    @ApiOperation(value = "Get today's festival")
    @GetMapping("/today")
    public JSONObject getFestival() throws ExecutionException, InterruptedException {
        long timestamp = new Date().getTime();
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        LocalDate date = instant.atZone(zoneId).toLocalDate();
        String isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("ISO date ==>" + isoDate);
        CollectionReference festivalsRef = festivalsService.getCollectionReference();
        Query query = festivalsRef.whereEqualTo("isoDate", isoDate).whereEqualTo("active", true);
        ApiFuture<QuerySnapshot> querySnapshotApiFuture = query.get();
        Festivals festival = null;
        for (DocumentSnapshot document : querySnapshotApiFuture.get().getDocuments()) {
            festival = document.toObject(Festivals.class);
            break;
        }
        JSONObject output = new JSONObject();
        output.put("festival", festival);
        System.out.println("REturrning ==>"+output);
        return output;
    }
}
