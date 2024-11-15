package com.startup.goHappy.entities.model;


import com.startup.goHappy.enums.VoucherStatusEnum;
import lombok.Data;

@Data
public class Vouchers {
    @DocumentId
    private String id;
    private String title;
    private String image;
    private double value;
    private int percent;
    private double limit;
    private String category;
    private String description; //convert to map
    private VoucherStatusEnum status;
}
