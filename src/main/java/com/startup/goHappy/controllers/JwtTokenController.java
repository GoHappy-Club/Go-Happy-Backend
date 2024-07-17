package com.startup.goHappy.controllers;

import com.startup.goHappy.services.OperationsTeamService;
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

        String role = operationsTeamService.authenticate(username, password);

        if (role != null && !role.isEmpty()) {
            final String jwt = jwtUtil.generateToken(username, role);
            return ResponseEntity.ok(Map.of("token", jwt));
        } else {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }
}