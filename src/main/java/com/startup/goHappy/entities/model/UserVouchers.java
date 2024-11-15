package com.startup.goHappy.entities.model;

import com.startup.goHappy.enums.VoucherStatusEnum;
import lombok.Data;

@Data
public class UserVouchers {
    @DocumentId
    private String id;
    private String voucherId;
    private String code;
    private String phone;
    private long expiryDate;
    private VoucherStatusEnum status;
}
