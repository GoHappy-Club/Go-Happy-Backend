package com.startup.goHappy.entities.TypecastedModels;

import com.startup.goHappy.entities.model.DocumentId;
import lombok.Data;

import java.util.List;


@Data
public class SearchEventDTO {

    @DocumentId
    private String id;

    private String eventName;

    private String eventDate;

    private String startTime;

    private String coverImage;

    private List<String> participantList;
}
