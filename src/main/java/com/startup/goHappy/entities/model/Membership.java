package com.startup.goHappy.entities.model;

import com.startup.goHappy.enums.MembershipEnum;
import lombok.Data;

import java.util.List;

@Data
public class Membership {
    @DocumentId
    private String id;

    private MembershipEnum membershipType;
    private Integer subscriptionFees;
    private Double rewardMultiplier;
    private Integer coinsPerMonth;
    private Integer discount;
    private Integer duration; // duration of membership in MONTHS
    private List<String> vouchers;
    private List<String> perks; // use unicode for bolds(already present in DB)
}
