package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class CoinPackages {
    @DocumentId
    private String id;
    private int coins;
    private String title;
    private int discountPercentage;
}
