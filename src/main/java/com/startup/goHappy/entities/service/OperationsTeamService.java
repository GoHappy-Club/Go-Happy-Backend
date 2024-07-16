package com.startup.goHappy.entities.service;

import com.startup.goHappy.entities.model.OperationsTeam;
import com.startup.goHappy.entities.repository.OperationsTeamRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class OperationsTeamService implements UserDetailsService {

    @Autowired
    private OperationsTeamRepository operationsTeamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<OperationsTeam> operationsTeamOptional = operationsTeamRepository.findById(username);

        if (!operationsTeamOptional.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        OperationsTeam operationsTeam = operationsTeamOptional.get();
        return new User(operationsTeam.getUsername(), operationsTeam.getPassword(), new ArrayList<>());
    }

    public boolean authenticate(@NotNull String username, String password) {
        try {
//            UserDetails userDetails = loadUserByUsername(username);
//            return passwordEncoder.matches(password, userDetails.getPassword());
            return username.equals("admin") && password.equals("admin");
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }
}