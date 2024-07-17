package com.startup.goHappy.controllers;

import com.startup.goHappy.services.UserRolesService;
import com.startup.goHappy.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JwtTokenController {

    @Autowired
    private UserRolesService userRolesService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> authenticationRequest) {
        String username = authenticationRequest.get("username");
        String password = authenticationRequest.get("password");
        List<String> roles = userRolesService.authenticate(username, password);

        if (roles != null && !roles.isEmpty()) {
            final String jwt = jwtUtil.generateToken(username, roles);
            return ResponseEntity.ok(Map.of("token", jwt));
        } else {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }
}