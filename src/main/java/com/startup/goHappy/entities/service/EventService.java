package com.startup.goHappy.entities.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;

@Service
public class EventService extends BaseService<Event> {
    @Autowired
    public void setEventRepository(EventRepository eventRepository) {
        this.setRepository(eventRepository);
    }
}
