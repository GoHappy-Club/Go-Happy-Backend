package com.startup.goHappy.entities.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.startup.goHappy.entities.model.Event;

import java.util.List;

public interface EventRepository extends ElasticsearchRepository<Event, String> {



}
