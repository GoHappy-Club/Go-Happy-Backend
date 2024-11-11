package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Templates;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TemplateRepository extends AbstractFirestoreRepository<Templates> {
    protected TemplateRepository(Firestore firestore) {
        super(firestore, "Templates");
    }
}
