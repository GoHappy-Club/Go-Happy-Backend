package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Vouchers;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VouchersRepository extends AbstractFirestoreRepository<Vouchers> {
    protected VouchersRepository(Firestore firestore) {
        super(firestore, "Vouchers");
    }
}
