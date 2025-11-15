package com.hcl.mi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hcl.mi.security.JwtAccessDeniedHandler;
import com.hcl.mi.security.JwtAuthenticationEntryPoint;
import com.hcl.mi.security.JwtAuthenticationFilter;
import com.hcl.mi.utils.ApplicationConstants;

@Configuration 
@EnableMethodSecurity 
public class SecurityConfig { 

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
 
    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter, 
            JwtAuthenticationEntryPoint entryPoint, 
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    } 
   
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	.cors()
        	.and()
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint)
            		.accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            	    .requestMatchers(HttpMethod.POST, "/api/v1/user/register", "/api/v1/user/login/**", "/user/refresh-token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/vendor/save").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/api/v1/vendor/edit").hasRole(ApplicationConstants.ADMIN_ROLE) 
                .requestMatchers(HttpMethod.DELETE, "/api/v1/vendor/delete/**").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers("/api/v1/vendor/**").hasAnyRole(ApplicationConstants.ADMIN_ROLE,ApplicationConstants.INSPECTOR_ROLE,
                		ApplicationConstants.USER_ROLE)

                .requestMatchers(HttpMethod.POST, "/api/v1/plant/save").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/api/v1/plant/edit").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/plant/delete/**").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers("/api/v1/plant/**").hasAnyRole(ApplicationConstants.ADMIN_ROLE,ApplicationConstants.INSPECTOR_ROLE,ApplicationConstants.USER_ROLE)

                .requestMatchers(HttpMethod.POST, "/api/v1/material/save").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/api/v1/material/edit").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/material/delete/**").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, "/api/v1/material/material-char/save").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/api/v1/ch/edit").hasRole(ApplicationConstants.ADMIN_ROLE)

                .requestMatchers("/api/v1/material/**").hasAnyRole(ApplicationConstants.ADMIN_ROLE,ApplicationConstants.INSPECTOR_ROLE,ApplicationConstants.USER_ROLE)
                
               

                .requestMatchers(HttpMethod.POST, "api/v1//inspection/create/lot").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.PUT, "/insp/lot/edit").hasRole(ApplicationConstants.ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, "/insp/save/lot/actu").hasRole(ApplicationConstants.INSPECTOR_ROLE)
                .requestMatchers(HttpMethod.PUT, "/insp/actu/edit").hasRole(ApplicationConstants.INSPECTOR_ROLE)
                .requestMatchers("/api/v1/inspection/**").hasAnyRole(ApplicationConstants.ADMIN_ROLE,ApplicationConstants.INSPECTOR_ROLE,ApplicationConstants.USER_ROLE)
 
                .requestMatchers("/api/v1/admin/**").hasRole(ApplicationConstants.ADMIN_ROLE)

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}