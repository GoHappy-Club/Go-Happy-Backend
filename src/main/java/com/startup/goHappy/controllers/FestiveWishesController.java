package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.model.Festivals;
import com.startup.goHappy.entities.repository.FestivalsRepository;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public void addFestivals(@RequestParam String year) throws JsonProcessingException, ExecutionException, InterruptedException {
        CollectionReference festiveRef = festivalsService.getCollectionReference();
        List<QueryDocumentSnapshot> lists = festiveRef.get().get().getDocuments();
        for (QueryDocumentSnapshot document : lists) {
            festivalsService.delete(document.toObject(Festivals.class));
        }
        String url = "https://calendarific.com/api/v2/holidays?api_key=" + apiKey + "&country=IN&year=" + year;
        String rawResponse = restTemplate.getForObject(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(rawResponse);

        JsonNode holidaysNode = rootNode.path("response").path("holidays");

        holidaysNode.forEach(holiday -> {
            try {
                Festivals festival = new Festivals();
                festival.setId(UUID.randomUUID().toString());
                festival.setName(holiday.path("name").asText());
                festival.setActive(false);
                festival.setIsoDate(holiday.path("date").path("iso").asText());
                festival.setTitle(holiday.path("description").asText());
                System.out.println("ISO DAE=>" + festival.getIsoDate());
                festival.setTimestamp(isoToUnix(festival.getIsoDate()));
                festivalsService.save(festival);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @ApiOperation(value = "Update festivals data (asset or active)")
    @PostMapping("/update")
    public void addFestivals(@RequestBody JSONObject params) {
        ObjectMapper objectMapper = new ObjectMapper();
        Festivals festival = objectMapper.convertValue(params, Festivals.class);
        festivalsService.save(festival);
    }

    @ApiOperation(value = "Get all festivals of the year")
    @PostMapping("/get")
    public List<Festivals> getAllFestivals(@RequestParam int page, @RequestBody(required = false) JSONObject params) throws ExecutionException, InterruptedException {
        final int LIMIT = 10;
        final int OFFSET = (page - 1) * LIMIT;
        CollectionReference festivalsRef = festivalsService.getCollectionReference();
        List<Festivals> festivals = new ArrayList<>();
        Query festivalQuery;

        if (params.containsKey("isoDates") && !params.getJSONArray("isoDates").isEmpty()) {
            festivalQuery = festivalsRef.whereIn("isoDate", params.getJSONArray("isoDates"));
        } else {
            festivalQuery = festivalsRef.whereGreaterThanOrEqualTo("timestamp", new Date().getTime() / 1000);
        }
        festivalQuery = festivalQuery.orderBy("timestamp", Query.Direction.ASCENDING).limit(LIMIT).offset(OFFSET);
        ApiFuture<QuerySnapshot> future = festivalQuery.get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            festivals.add(document.toObject(Festivals.class));
        }
        return festivals;
    }

    @ApiOperation(value = "Get today's festival")
    @GetMapping("/today")
    public JSONObject getFestival() throws ExecutionException, InterruptedException {
        long timestamp = new Date().getTime();
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        LocalDate date = instant.atZone(zoneId).toLocalDate();
        String isoDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
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
        return output;
    }

    private Long isoToUnix(String isoDate) {
        return LocalDate.parse(isoDate)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .getEpochSecond();

    }
}
