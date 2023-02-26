package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class PaymentLog {
	@DocumentId
	private String id;
	private String phone;
	private String paymentDate;
	private Integer amount;
}
