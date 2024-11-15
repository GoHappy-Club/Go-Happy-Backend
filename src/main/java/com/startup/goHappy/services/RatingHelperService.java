package com.startup.goHappy.services;

import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.repository.RatingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static com.google.cloud.firestore.AggregateField.average;
import static com.google.cloud.firestore.AggregateField.count;

@Service
public class RatingHelperService {

    @Autowired
    RatingsRepository ratingsService;

    public double getRatingByCategory(String category) throws ExecutionException, InterruptedException {
        CollectionReference ratingRef = ratingsService.getCollectionReference();
        Query query = ratingRef.whereEqualTo("subCategory",category);
        AggregateQuerySnapshot snapshot = query.aggregate(average("rating")).get().get();
        return snapshot.get(average("rating"));
    }
}