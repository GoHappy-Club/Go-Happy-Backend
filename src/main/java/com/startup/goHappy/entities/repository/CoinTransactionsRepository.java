package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.CoinTransactions;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Service;

@Service
public class CoinTransactionsRepository extends AbstractFirestoreRepository<CoinTransactions> {
    protected CoinTransactionsRepository(Firestore firestore) {
        super(firestore, "CoinTransactions");
    }
}
