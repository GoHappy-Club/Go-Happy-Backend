package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Video;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VideoRepository extends AbstractFirestoreRepository<Video> {
    protected VideoRepository(Firestore firestore) {
        super(firestore, "Videos");
    }
}
