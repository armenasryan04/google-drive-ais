package com.samplemission.collectcvsfromgoogledrive.config.web;

import com.samplemission.collectcvsfromgoogledrive.config.web.filter.JWTAuthenticationTokenFilter;
import com.samplemission.collectcvsfromgoogledrive.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JWTAuthenticationTokenFilter jwtAuthenticationTokenFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .cors()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeHttpRequests()
        .antMatchers(HttpMethod.POST, "/user/auth")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/webhook")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/webhook")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/user/register")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/api/submit")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/user/responsible")
        .authenticated()
        .anyRequest()
        .authenticated();
    http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return authenticationProvider;
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/notifications")
            .allowedOrigins("https://cf06-185-215-54-147.ngrok-free.app") // Укажите свой URL
            .allowedMethods("POST", "GET")
            .allowedHeaders("*");
      }
    };
  }
}
