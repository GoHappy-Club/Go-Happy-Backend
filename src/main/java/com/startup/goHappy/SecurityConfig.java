package com.startup.goHappy;

import com.google.common.collect.ImmutableList;
import com.startup.goHappy.services.UserRolesService;
import com.startup.goHappy.services.CustomPasswordEncoder;
import com.startup.goHappy.utils.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${adminDashboard.url}")
    private String adminDashboardUrl;

    @Autowired
    private UserRolesService userRolesService;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userRolesService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CustomPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(adminDashboardUrl.split(",")));
        configuration.setAllowedMethods(ImmutableList.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(ImmutableList.of("*"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(corsConfigurationSource()).and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/_ah/start").permitAll()
                .antMatchers("/authenticate").permitAll()
                .antMatchers("/paytring/result").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/properties/list").permitAll()
                .antMatchers("/user/setPaymentDataWorkshop", "/user/setPaymentDataContribution").permitAll()
                .antMatchers( "/membership/buy","/membership/renew","/membership/upgrade","/membership/topUp").permitAll()
                .antMatchers("/user/download", "/user/topReferrals","/user/getUserByEmail").hasAnyRole("USER_DATA_MANAGER", "ADMIN")
                .antMatchers("/payments/download").hasAnyRole("ADMIN", "PAYMENT_MANAGER")
                .antMatchers("/event/create", "/event/delete", "/admin/events/updateEvent/{id}").hasAnyRole("ADMIN", "EVENT_MANAGER")
                .antMatchers("/admin/tambola/**").hasAnyRole("TAMBOLA_MANAGER", "ADMIN")
                .antMatchers("/notifications/**").hasAnyRole("ADMIN", "NOTIFICATION_MANAGER")
                .antMatchers("/trips/add").hasAnyRole("ADMIN", "TRIP_MANAGER")
                .anyRequest().hasAnyRole("ADMIN", "USER", "TRIP_MANAGER", "TAMBOLA_MANAGER", "EVENT_MANAGER", "PAYMENT_MANAGER", "NOTIFICATION_MANAGER","USER_DATA_MANAGER");

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}