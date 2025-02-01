package com.startup.goHappy.entities.model;


import com.startup.goHappy.enums.MembershipEnum;
import com.startup.goHappy.enums.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserMemberships {

    @DocumentId
    private String id;

    private MembershipEnum membershipType = MembershipEnum.Free;
    private String userId;
    private String phone;
    private String membershipStartDate;
    private String membershipEndDate;
    private int coins = 0;
    private String cancellationDate;
    private String cancellationReason;
    private String lastCoinsCreditedDate;
    private boolean freeTrialUsed = true;
    private boolean freeTrialActive = false;
}
