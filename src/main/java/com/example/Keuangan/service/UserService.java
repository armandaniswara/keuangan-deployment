package com.example.Keuangan.service;

import com.example.Keuangan.entity.User;
import com.example.Keuangan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        user.setId(null);
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public boolean verifyLogin(String rawPassword, String encodePasswordFromDB){
        return passwordEncoder.matches(rawPassword, encodePasswordFromDB);
    }
}