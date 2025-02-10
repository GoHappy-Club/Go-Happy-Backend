package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Reminder;
import com.startup.goHappy.entities.repository.ReminderRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/reminders")
public class RemindersController {
    @Autowired
    ReminderRepository reminderService;

    @ApiOperation(value = "Fetch reminders by phone number")
    @GetMapping("/get")
    public JSONObject getReminders(@RequestParam String phone) throws ExecutionException, InterruptedException {
        List<Reminder> reminders = getRemindersByPhone(phone);
        JSONObject result = new JSONObject();
        result.put("reminders", reminders);
        return result;
    }

    @ApiOperation(value = "Add a new reminder to user")
    @PostMapping("/add")
    public JSONObject addNewReminder(@RequestBody Reminder reminder) throws ExecutionException, InterruptedException {
        reminder.setId(UUID.randomUUID().toString());
        reminderService.save(reminder);
        List<Reminder> newReminders = getRemindersByPhone(reminder.getPhone());
        return new JSONObject() {{
            put("success", true);
            put("reminders", newReminders);
        }};
    }

    @ApiOperation(value = "Delete a reminder")
    @PostMapping("/delete")
    public void deleteReminder(@RequestBody Reminder reminder) throws ExecutionException, InterruptedException {
        reminderService.delete(reminder);
    }

    private List<Reminder> getRemindersByPhone(String phone) throws ExecutionException, InterruptedException {
        CollectionReference remindersRef = reminderService.getCollectionReference();
        Query query = remindersRef.whereEqualTo("phone", phone);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Reminder> reminders = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get()) {
            Reminder reminder = document.toObject(Reminder.class);
            reminders.add(reminder);
        }
        return reminders;
    }
}
