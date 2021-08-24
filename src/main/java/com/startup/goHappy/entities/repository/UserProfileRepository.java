package com.startup.goHappy.entities.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;

import java.util.List;

public interface UserProfileRepository extends ElasticsearchRepository<UserProfile, String> {



}
