package com.reimburse.expenseservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
// Tells Spring to turn on the automatic auditing feature
// and use the "auditorProvider" bean defined below to find the current user
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider(){
        // lambda makes it so it creates an anonymous class which implements auditorAware
        // and overrides the getCurrentAuditor with the body code we provide
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken){
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
