package com.startup.goHappy.entities.model;


import com.startup.goHappy.enums.MembershipEnum;
import lombok.Data;

@Data
public class UserMemberships {

    @DocumentId
    private String id;

    private MembershipEnum membershipType=MembershipEnum.Free;
    private String userId;
    private String phone;
    private String membershipStartDate;
    private String membershipEndDate;
    private int coins = 0;
    private String cancellationDate;
    private String cancellationReason;
    private Object vouchers;
}
