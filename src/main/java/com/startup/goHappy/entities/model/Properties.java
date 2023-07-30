package com.startup.goHappy.entities.model;

import kotlin.jvm.Transient;
import lombok.Data;

import java.util.List;


@Data
public class Properties {
    @DocumentId
    private String id;
    private String whatsappLink;
}
