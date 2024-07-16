package com.startup.goHappy.entities.repository;


import com.startup.goHappy.entities.model.OperationsTeam;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.Firestore;

@Repository
public class OperationsTeamRepository extends AbstractFirestoreRepository<OperationsTeam> {
    protected OperationsTeamRepository(Firestore firestore) {
        super(firestore, "OperationsTeam");
    }
}