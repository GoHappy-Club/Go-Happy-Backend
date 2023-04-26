package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Properties;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class PropertiesRepository extends AbstractFirestoreRepository<Properties> {
    protected PropertiesRepository(Firestore firestore) {
        super(firestore, "Properties");
    }
}