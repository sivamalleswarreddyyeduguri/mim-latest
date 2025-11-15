package com.hcl.mi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.hcl.mi.entities.User;
import com.hcl.mi.repositories.UserRepository;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(info = @Info(title = "Material Inspection Service", 
                                description = "Capturing Inspection Actuals details.",
                                contact = @Contact(name = "Siva", 
                                email = "siva@hcltech.com")))
@Slf4j
public class MaterialInspectionRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaterialInspectionRestApplication.class, args);
    } 

    @Bean  
    CommandLineRunner initDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = User.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode("Admin@123"))
                        .email("admin@hcltech.com")
                        .mobileNum("8639054306")
                        .role("ADMIN")
                        .status("Active") 
                        .build();
                userRepository.save(admin);
                log.info("Default admin created -> username: admin / password: Admin@123");
            }
        }; 
    }
}