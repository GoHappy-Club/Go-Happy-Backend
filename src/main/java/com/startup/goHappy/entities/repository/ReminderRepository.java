package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Reminder;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReminderRepository extends AbstractFirestoreRepository<Reminder> {
    protected ReminderRepository(Firestore firestore) {
        super(firestore, "Reminders");
    }
}
