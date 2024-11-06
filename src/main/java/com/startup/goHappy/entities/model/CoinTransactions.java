package com.startup.goHappy.entities.model;

import com.startup.goHappy.enums.TransactionTypeEnum;
import lombok.Data;

@Data
public class CoinTransactions {
    @DocumentId
    private String id;
    private String phone;
    private String source;
    private String sourceId;
    private int amount;
    private TransactionTypeEnum type;   // CREDIT or DEBIT
    private long transactionDate;
    private String title;
}
