package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.PaymentLog;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class PaymentLogRepository extends AbstractFirestoreRepository<PaymentLog> {
    protected PaymentLogRepository(Firestore firestore) {
        super(firestore, "PaymentLogs");
    }
}