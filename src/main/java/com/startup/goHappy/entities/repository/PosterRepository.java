package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Poster;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class PosterRepository extends AbstractFirestoreRepository<Poster> {
    protected PosterRepository(Firestore firestore) {
        super(firestore, "Poster");
    }
}