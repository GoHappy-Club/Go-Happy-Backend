package com.startup.goHappy.entities.model;


import com.startup.goHappy.enums.VoucherStatusEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Vouchers {
    @DocumentId
    private String id;
    private String title;
    private String image;
    private Double value;
    private Integer percent;
    private Double limit;
    private String category;
    private Map<String, Object> description = new HashMap<>() {{
        put("redemption", new ArrayList<>(List.of("first first first first", "second second second second ")));
        put("tnc", new ArrayList<>(List.of("first first first first", "second second second second ")));
    }};
    private VoucherStatusEnum status;
}
