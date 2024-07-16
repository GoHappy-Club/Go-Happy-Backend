package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class OperationsTeam {
    @DocumentId
    private String username;
    private String password;
}
