package com.startup.goHappy.entities.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;

@Service
public class UserProfileService extends BaseService<UserProfile> {
    @Autowired
    public void setUserProfileRepository(UserProfileRepository userProfileRepository) {
        this.setRepository(userProfileRepository);
    }
}
