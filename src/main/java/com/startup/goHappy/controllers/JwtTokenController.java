package com.startup.goHappy.controllers;

import com.startup.goHappy.entities.service.OperationsTeamService;
import com.startup.goHappy.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwtTokenController {

    @Autowired
    private OperationsTeamService operationsTeamService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> authenticationRequest) {
        String username = authenticationRequest.get("username");
        String password = authenticationRequest.get("password");

        if (operationsTeamService.authenticate(username, password)) {
            final String jwt = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of("token", jwt));
        } else {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }
}