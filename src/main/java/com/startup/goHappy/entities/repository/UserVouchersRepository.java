package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.UserVouchers;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserVouchersRepository extends AbstractFirestoreRepository<UserVouchers> {
    protected UserVouchersRepository(Firestore firestore) {
        super(firestore,"UserVouchers");
    }
}
