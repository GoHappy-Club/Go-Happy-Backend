package com.startup.goHappy.admin.controllers;


import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.Templates;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.entities.repository.TemplateRepository;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    @Autowired
    EventRepository eventService;
    @Autowired
    TemplateRepository templateService;

    @ApiOperation(value = "To get already created templates")
    @GetMapping("/getTemplates")
    public List<Templates> getTemplates() {
        Iterable<Templates> iterableTemplates = templateService.retrieveAll();
        return IterableUtils.toList(iterableTemplates);
    }

    @ApiOperation(value = "To create a new params")
    @PostMapping("/createTemplate")
    public @ResponseBody JSONObject createTemplate(@RequestBody JSONObject params) {
        Templates templateNew = new Templates();
        templateNew.setId(UUID.randomUUID().toString());
        if (params.containsKey("templateName")) {
            templateNew.setTemplateName(params.getString("templateName"));
        }if (params.containsKey("eventName")) {
            templateNew.setEventName(params.getString("eventName"));
        }if (params.containsKey("coverImage")) {
            templateNew.setCoverImage(params.getString("coverImage"));
        }if (params.containsKey("description")) {
            templateNew.setDescription(params.getString("description"));
        }if (params.containsKey("beautifulDescription")) {
            templateNew.setBeautifulDescription(params.getString("beautifulDescription"));
        }if (params.containsKey("expertImage")) {
            templateNew.setExpertImage(params.getString("expertImage"));
        }if (params.containsKey("expertName")) {
            templateNew.setExpertName(params.getString("expertName"));
        }if (params.containsKey("category")) {
            templateNew.setCategory(params.getString("category"));
        }if (params.containsKey("creator")) {
            templateNew.setCreator(params.getString("creator"));
        }if (params.containsKey("eventDate")) {
            templateNew.setEventDate(params.getString("eventDate"));
        }if (params.containsKey("startTime")) {
            templateNew.setStartTime(params.getString("startTime"));
        }if (params.containsKey("endTime")) {
            templateNew.setEndTime(params.getString("endTime"));
        }if (params.containsKey("costType")) {
            templateNew.setCostType(params.getString("costType"));
        }if (params.containsKey("cost")) {
            templateNew.setCost(params.getInteger("cost"));
        }if (params.containsKey("seatsLeft")) {
            templateNew.setSeatsLeft(params.getInteger("seatsLeft"));
        }if (params.containsKey("occurance")) {
            templateNew.setOccurance(params.getString("occurance"));
        }if (params.containsKey("type")) {
            templateNew.setType(params.getString("type"));
        }if (params.containsKey("isParent")) {
            templateNew.setIsParent(params.getBoolean("isParent"));
        }if (params.containsKey("isScheduled")) {
            templateNew.setIsScheduled(params.getBoolean("isScheduled"));
        }if (params.containsKey("cron")) {
            templateNew.setCron(params.getString("cron"));
        }if (params.containsKey("sameDayEventId")) {
            templateNew.setSameDayEventId(params.getString("sameDayEventId"));
        }
        templateService.save(templateNew);
        JSONObject output = new JSONObject();
        output.put("success",true);
        return output;
    }

    @ApiOperation(value = "To get paginated events data")
    @GetMapping("getPaginatedEvents")
    public ResponseEntity<?> getPaginatedEvents(
            @RequestParam int page,
            @RequestParam(required = false) String minDate,
            @RequestParam(required = false) String maxDate,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) String recordingNull) {
        final int LIMIT = 10;
        final int OFFSET = (page - 1) * LIMIT;

        try {
            CollectionReference eventRef = eventService.getCollectionReference();
            Query query = eventRef.whereEqualTo("isParent", false);

            if (recordingNull != null && !recordingNull.isEmpty()) {
                if (recordingNull.equals("true")) {
                    query = query.whereEqualTo("recordingLink", null);
                }
            }

            if (minDate != null && !minDate.isEmpty() && maxDate != null && !maxDate.isEmpty()) {
                query = query.whereGreaterThanOrEqualTo("startTime", minDate).whereLessThan("startTime", maxDate);
            }

            if (filterField != null && !filterField.isEmpty() && filterValue != null && !filterValue.isEmpty()) {
                query = query.whereEqualTo(filterField, filterValue);
            }
            query = query.orderBy("eventDate", Query.Direction.DESCENDING)
                    .orderBy("startTime", Query.Direction.DESCENDING);
            query = query.limit(LIMIT).offset(OFFSET);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<Event> events = new ArrayList<>();

            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                Event event = document.toObject(Event.class);
                if (event != null) {
                    events.add(event);
                }
            }
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching events: " + e.getMessage());
        }
    }

    @ApiOperation(value = "To update an event")
    @PostMapping(value = "/updateEvent/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Map<String, String>> updateEvent(@PathVariable("id") String eventId, @RequestBody Map<String, Object> updatedFields) {
        CollectionReference eventRef = eventService.getCollectionReference();
        eventRef.document(eventId).update(updatedFields);
        Map<String, String> output = new HashMap<>();
        output.put("status", "success");
        return ResponseEntity.ok(output);
    }
}
