package com.mvcjavacode.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.mvcjavacode.exception.UserNotFoundException;
import com.mvcjavacode.model.entity.User;
import com.mvcjavacode.model.view.Views;
import com.mvcjavacode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private static String USER_NOT_FOUND = "User not found";

    @GetMapping({"", "/"})
    @JsonView(Views.UserSummary.class)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{userId}")
    @JsonView(Views.UserDetail.class)
    public Optional<User> getUserById(@PathVariable Long userId) {
        return Optional.ofNullable(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND)));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Validated @RequestBody User user) {
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Validated @RequestBody User user) {
        Optional<User> userOptional = Optional.of(userRepository.findById(id))
                .orElseThrow(()->new UserNotFoundException(USER_NOT_FOUND));

        User userToUpdate = userOptional.get();
        userToUpdate.setName(user.getName());
        userToUpdate.setEmail(user.getEmail());

        User updatedUser = userRepository.save(userToUpdate);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }

        userRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
