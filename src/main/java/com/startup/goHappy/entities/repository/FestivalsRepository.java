package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Festivals;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FestivalsRepository extends AbstractFirestoreRepository<Festivals> {
    protected FestivalsRepository(Firestore firestore) {
        super(firestore,"Festivals");
    }
}
