package com.startup.goHappy.admin.controllers;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.json.JSONObject;
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
