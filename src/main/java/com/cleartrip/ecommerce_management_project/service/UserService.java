package com.cleartrip.ecommerce_management_project.service;
import com.cleartrip.ecommerce_management_project.model.User;
import com.cleartrip.ecommerce_management_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // create user
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // login user
    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    // get user by id
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
