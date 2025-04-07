package com.nusiss.orderservice.controller;

import com.nusiss.orderservice.config.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*import java.util.List;
import java.util.Optional;*/
@RestController
public class DemoController {

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> getAllUsers() {
        return ResponseEntity.ok(new ApiResponse<String>(true, "test successfully", ""));
    }
    /*@GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", value)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "User created successfully", savedUser));
    }

//    @PostMapping
//    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user, @RequestBody List<Address> addresses) {
//        User savedUser = userService.saveUser(user, addresses);  // 修改：传递地址列表
//        return ResponseEntity.status(201).body(new ApiResponse<>(true, "User and address created successfully", savedUser));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            updatedUser.setUserId(id);
            User savedUser = userService.saveUser(updatedUser);
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", savedUser));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null));
        }
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Integer id) {
//        Optional<User> existingUser = userService.getUserById(id);
//        if (existingUser.isPresent()) {
//            userService.deleteUser(id);
//            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
//        } else {
//            return ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null));
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Integer id) {
        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User and address deleted successfully", null));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>(false, "User not found", null));
        }
    }*/
}
