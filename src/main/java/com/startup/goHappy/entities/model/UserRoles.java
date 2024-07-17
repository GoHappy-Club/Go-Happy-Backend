package com.startup.goHappy.entities.model;

import lombok.Data;

import java.util.List;

@Data
public class UserRoles {
    @DocumentId
    private String username;
    private String password;
    private List<String> roles;
}
