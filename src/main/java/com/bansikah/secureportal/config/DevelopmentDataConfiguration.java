package com.bansikah.secureportal.config;

import com.bansikah.secureportal.user.domain.Role;
import com.bansikah.secureportal.user.domain.UserAccount;
import com.bansikah.secureportal.user.repository.UserAccountRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
class DevelopmentDataConfiguration {

    @Bean
    CommandLineRunner developmentUsers(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        return arguments -> {
            if (users.count() == 0) {
                users.save(new UserAccount("user", "user@secureportal.local", passwordEncoder.encode("change-me-user"), Set.of(Role.USER)));
                users.save(new UserAccount("admin", "admin@secureportal.local", passwordEncoder.encode("change-me-admin"), Set.of(Role.ADMIN)));
            }
        };
    }
}