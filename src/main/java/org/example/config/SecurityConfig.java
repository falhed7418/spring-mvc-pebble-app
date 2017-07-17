package org.example.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.example.controller.CustomAccessDeniedHandler;
import org.example.model.PersistenceObjectDAO;
import org.example.model.PersistenceObjectDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean(name = "entityManager")
    public EntityManager entityManager() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("persmodel");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        return entityManager;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        authenticationManagerBuilder.userDetailsService()
//        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryUserDetailsManagerConfigurer = authenticationManagerBuilder.inMemoryAuthentication();
//        inMemoryUserDetailsManagerConfigurer.withUser("admin").password("admin").roles("ADMIN");
        authenticationManagerBuilder.authenticationProvider(new CustomAuthenticationProvider(entityManager()));
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeRequests()
                .antMatchers("/secured/**")
                .access("hasRole('ADMIN') or hasRole('MANAGER')");

        httpSecurity
                .formLogin()
                .loginPage("/login")
                .failureUrl("/login?error")
                .loginProcessingUrl("/login")
                .usernameParameter("app_username")
                .passwordParameter("app_password")
                .successHandler(new SavedRequestAwareAuthenticationSuccessHandler());

        httpSecurity
                .requiresChannel()
//                .anyRequest()
                .antMatchers("/secured/**", "/login", "/logout")
                .requiresSecure();

        httpSecurity
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler());
//        httpSecurity.exceptionHandling().accessDeniedPage("/403")

//        httpSecurity
//                .rememberMe()
//                .key("rem-me-key")
//                .rememberMeParameter("remember-me-param")
//                .rememberMeCookieName("my-remember-me")
//                .tokenValiditySeconds(350);
    }
}