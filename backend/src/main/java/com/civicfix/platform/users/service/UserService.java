package com.civicfix.platform.users.service;

import com.civicfix.platform.common.api.PagedResponse;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.departments.repository.DepartmentRepository;
import com.civicfix.platform.organizations.repository.OrganizationRepository;
import com.civicfix.platform.users.dto.CreateUserRequest;
import com.civicfix.platform.users.dto.UserDto;
import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.enums.RoleType;
import com.civicfix.platform.users.repository.RoleRepository;
import com.civicfix.platform.users.repository.UserRepository;
import com.civicfix.platform.wards.repository.WardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       OrganizationRepository organizationRepository,
                       DepartmentRepository departmentRepository,
                       WardRepository wardRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.wardRepository = wardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getUsers(Pageable pageable) {
        Page<UserDto> page = userRepository.findAllByActiveTrue(pageable).map(UserDto::fromEntity);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getOrganizationId() != null) {
            user.setOrganization(organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getOrganizationId())));
        }
        if (request.getDepartmentId() != null) {
            user.setDepartment(departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId())));
        }
        if (request.getWardId() != null) {
            user.setWard(wardRepository.findById(request.getWardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ward", "id", request.getWardId())));
        }

        Set<Role> roles = new HashSet<>();
        for (RoleType roleType : request.getRoles()) {
            Role role = roleRepository.findByName(roleType)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleType.name()));
            roles.add(role);
        }
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return UserDto.fromEntity(saved);
    }
}
