//package com.food.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import com.food.security.jwt.AuthEntryPointJwt;
//import com.food.security.jwt.AuthTokenFilter;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private AuthTokenFilter authTokenFilter;
//
//    @Autowired
//    private UserDetailsService userDetailsServiceImpl;
//
//    @Autowired
//    private AuthEntryPointJwt unauthorizedHandler;
//
//    @Bean
//    public static PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    private static final String[] AUTH_WHITELIST = { 
//        "/swagger-ui/**", 
//        "/v2/api-docs", 
//        "/v3/api-docs/**",
//        "/configuration/ui", 
//        "/swagger-resources/**", 
//        "/configuration/security", 
//        "/swagger-ui.html", 
//        "/webjars/**",
//        "/css/**", 
//        "/js/**", 
//        "/images/**" 
//    };
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors().and().csrf().disable()
//            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            .and()
//            .authorizeRequests()
//                .requestMatchers(AUTH_WHITELIST).permitAll() 
//                .requestMatchers("/**").permitAll() 
//                .anyRequest().authenticated();
//        
//        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//}
