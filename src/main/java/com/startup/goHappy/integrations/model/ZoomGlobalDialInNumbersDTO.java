package com.startup.goHappy.integrations.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class ZoomGlobalDialInNumbersDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String country;

    private String country_name;

    private String city;

    private String number;

    private String type;

}
