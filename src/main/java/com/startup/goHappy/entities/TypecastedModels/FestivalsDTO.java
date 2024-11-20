package com.startup.goHappy.entities.TypecastedModels;

import com.startup.goHappy.entities.model.DocumentId;
import lombok.Data;

@Data
public class FestivalsDTO {
    @DocumentId
    private String id;
}
