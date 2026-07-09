package com.kiosk.branch.security;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {


	@Bean
	SecurityFilterChain filterChain(
	        HttpSecurity http,
	        JwtUtil jwtUtil
	) throws Exception {


	    http
	        .csrf(csrf -> csrf.disable())

	        .addFilterBefore(
	            new JwtFilter(jwtUtil),
	            UsernamePasswordAuthenticationFilter.class
	        )

	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/branch/login")
	            .permitAll()

	            .requestMatchers("/branch/**")
	            .authenticated()

	            .anyRequest()
	            .permitAll()
	        );


	    return http.build();
	}

}