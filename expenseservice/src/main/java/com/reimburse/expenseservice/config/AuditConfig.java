package com.reimburse.expenseservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
        return () -> Optional.of("system");
    }
}
