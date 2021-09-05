package com.startup.goHappy.integrations.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class ZoomInterpreterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public String email;

    public String languages;
}
