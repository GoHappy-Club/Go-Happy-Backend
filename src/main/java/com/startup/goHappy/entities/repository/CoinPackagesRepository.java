package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.CoinPackages;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CoinPackagesRepository extends AbstractFirestoreRepository<CoinPackages> {
    protected CoinPackagesRepository(Firestore firestore) {
        super(firestore, "CoinPackages");
    }
}
