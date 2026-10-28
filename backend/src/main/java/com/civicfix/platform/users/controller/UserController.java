package com.civicfix.platform.users.controller;

import com.civicfix.platform.common.api.ApiResponse;
import com.civicfix.platform.common.api.PageRequestBuilder;
import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.users.dto.CreateUserRequest;
import com.civicfix.platform.users.dto.UserDto;
import com.civicfix.platform.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<PagedResponse<UserDto>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        Pageable pageable = PageRequestBuilder.build(page, size, sortBy, sortDirection);
        return ApiResponse.ok(userService.getUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN', 'OFFICER')")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DEPT_ADMIN')")
    public ApiResponse<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.created("User created successfully", userService.createUser(request));
    }
}
