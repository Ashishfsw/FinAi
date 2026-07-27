package com.fintech.portfolio.config;

import com.fintech.portfolio.entity.User;
import com.fintech.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DefaultUserInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDefaultUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("alpha_trader").isEmpty()) {
                User user = User.builder()
                        .username("alpha_trader")
                        .email("alpha@finai.local")
                        .fullName("Alpha Trader")
                        .password(passwordEncoder.encode("alpha123"))
                        .build();
                userRepository.save(user);
            }
        };
    }
}
