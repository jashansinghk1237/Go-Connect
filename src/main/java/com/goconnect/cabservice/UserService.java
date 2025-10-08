package com.goconnect.cabservice;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void updateUserBalance(String username, Double newBalance) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setGoMoneyBalance(newBalance);
            userRepository.save(user);
        }
    }

    public User createUser(String username, String password, String role, Double goMoneyBalance) {
        User user = new User(username, password, role, goMoneyBalance);
        return userRepository.save(user);
    }
}