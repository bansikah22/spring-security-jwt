package com.bansikah.secureportal.security.authentication;

import com.bansikah.secureportal.user.domain.Role;
import com.bansikah.secureportal.user.domain.UserAccount;
import com.bansikah.secureportal.user.repository.UserAccountRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public PortalUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount account = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        for (Role role : account.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            role.permissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.name())));
        }
        return org.springframework.security.core.userdetails.User.withUsername(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities)
                .disabled(!account.isEnabled())
                .accountLocked(account.isAccountLocked())
                .build();
    }
}