package com.startup.goHappy;

import com.startup.goHappy.services.UserRolesService;
import com.startup.goHappy.services.CustomPasswordEncoder;
import com.startup.goHappy.utils.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/authenticate").permitAll()
                .antMatchers("/user/setPaymentDataWorkshop", "/user/setPaymentDataContribution").permitAll()
                .antMatchers("/payments/download").hasAnyRole("ADMIN", "PAYMENT_MANAGER")
                .antMatchers("/event/create", "/event/delete").hasAnyRole("ADMIN", "EVENT_MANAGER")
                .antMatchers("/notifications/**").hasAnyRole("ADMIN", "NOTIFICATION_MANAGER")
                .antMatchers("/trips/add").hasAnyRole("ADMIN", "TRIP_MANAGER")
                .anyRequest().hasAnyRole("ADMIN", "USER")
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}