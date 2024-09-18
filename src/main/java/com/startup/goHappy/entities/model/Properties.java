package com.startup.goHappy.entities.model;

import kotlin.jvm.Transient;
import lombok.Data;

import java.util.List;


@Data
public class Properties {
    @DocumentId
    private String id;
    private List<String> whatsappGroupLink; // 0-> official , 1-> homework
    private String whatsappHelpLink; // for get help button
    private String appVersion;
    private Boolean forceUpdate;
    private int buildNumber;
}