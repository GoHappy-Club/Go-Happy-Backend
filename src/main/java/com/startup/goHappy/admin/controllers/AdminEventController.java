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
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Event> getPaginatedEvents(@RequestParam int page) throws ExecutionException, InterruptedException {
        int LIMIT = 20;
        int OFFSET = (page - 1) * 20;
        CollectionReference eventRef = eventService.getCollectionReference();
        Query query = eventRef.whereEqualTo("isParent", false).orderBy("eventDate", Query.Direction.DESCENDING).limit(LIMIT).offset(OFFSET);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        Set<Event> eventsNew = new HashSet<>();
        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                eventsNew.add(document.toObject(Event.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Event> eventsNewBest = IterableUtils.toList(eventsNew);
        eventsNewBest.sort(Comparator.comparing(Event::getStartTime).reversed());

        return eventsNewBest;
    }
}
