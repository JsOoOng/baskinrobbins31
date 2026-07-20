package com.kiosk.customer.order.controller;

import com.kiosk.customer.order.repository.UserRepository;
import com.kiosk.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{phone}/points")
    public ResponseEntity<Integer> getUserPoints(@PathVariable String phone) {
        Optional<User> userOptional = userRepository.findByPhone(phone);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok(user.getPointBalance() != null ? user.getPointBalance() : 0);
        }
        return ResponseEntity.ok(0); // If user doesn't exist, they have 0 points
    }
}
