package com.goconnect.cabservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.goconnect.cabservice.model.User;
import com.goconnect.cabservice.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default user if it doesn't exist
        if (!userRepository.existsByUsername("123")) {
            User defaultUser = new User();
            defaultUser.setUsername("123");
            defaultUser.setPassword(passwordEncoder.encode("123"));
            defaultUser.setRole("ROLE_USER");
            defaultUser.setGoMoneyBalance(1000000.0);
            userRepository.save(defaultUser);
            System.out.println("Default user '123' created with password '123' and balance 1,000,000");
        }
    }
}
