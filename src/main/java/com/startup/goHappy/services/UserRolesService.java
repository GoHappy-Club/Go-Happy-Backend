package com.startup.goHappy.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.model.UserRoles;
import com.startup.goHappy.entities.repository.UserRolesRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserRolesService implements UserDetailsService {

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CollectionReference userRoles = userRolesRepository.getCollectionReference();
        Query usernameQuery = userRoles.whereEqualTo("username", username);
        ApiFuture<QuerySnapshot> querySnapshot1 = usernameQuery.get();
        try {
            List<QueryDocumentSnapshot> documents = querySnapshot1.get().getDocuments();

            if (documents.isEmpty()) {
                throw new UsernameNotFoundException("User not found with username: " + username);
            }

            DocumentSnapshot document = documents.get(0);

            String password = document.getString("password");
            List<String> roles = (List<String>) document.get("roles");


            if (password == null || roles == null) {
                throw new UsernameNotFoundException("User data is incomplete for username: " + username);
            }

            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            return new User(username, password, authorities);

        } catch (InterruptedException | ExecutionException e) {
            throw new UsernameNotFoundException("Error fetching user data", e);
        }
    }

    public List<String> authenticate(@NotNull String username, String password) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            if (passwordEncoder.matches(password, userDetails.getPassword())) {
                return userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(role -> role.replace("ROLE_", ""))
                        .collect(Collectors.toList());
            }
        } catch (UsernameNotFoundException e) {
            System.out.println("User not found: " + e.getMessage());
        }
        return null;
    }
}