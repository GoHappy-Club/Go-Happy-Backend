package com.startup.goHappy.entities.repository;


import com.startup.goHappy.entities.model.UserRoles;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.Firestore;

@Repository
public class UserRolesRepository extends AbstractFirestoreRepository<UserRoles> {
    protected UserRolesRepository(Firestore firestore) {
        super(firestore, "UserRoles");
    }
}