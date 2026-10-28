package com.civicfix.platform.security;

import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.entity.User;
import com.civicfix.platform.users.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        // Evaluate lock status and check for automatic lockout expiration
        boolean nonLocked = user.isAccountNonLocked();
        if (nonLocked && user.isAccountLocked()) {
            userRepository.save(user); // Persist auto-unlocked status
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> roleNames = new HashSet<>();
        Set<String> permissionNames = new HashSet<>();

        for (Role role : user.getRoles()) {
            String rName = role.getName().name();
            roleNames.add(rName);
            authorities.add(new SimpleGrantedAuthority(rName));

            if (role.getPermissions() != null) {
                for (var perm : role.getPermissions()) {
                    String pName = perm.getName().name();
                    permissionNames.add(pName);
                    authorities.add(new SimpleGrantedAuthority(pName));
                }
            }
        }

        Long orgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        Long wardId = user.getWard() != null ? user.getWard().getId() : null;

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                orgId,
                deptId,
                wardId,
                user.isActive(),
                !nonLocked,
                user.isEmailVerified(),
                roleNames,
                permissionNames,
                authorities
        );
    }
}
