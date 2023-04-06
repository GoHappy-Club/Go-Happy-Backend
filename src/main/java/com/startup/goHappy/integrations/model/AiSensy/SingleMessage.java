package com.startup.goHappy.integrations.model.AiSensy;

import lombok.Data;

import java.util.List;

@Data
public class SingleMessage {
    private String campaignName;
    private List<String> templateParams;
    private String destination;
    private String userName;
}
