package org.example.config;

import org.example.model.Role;
import org.example.model.User;
import org.example.model.UserDAO;
import org.example.model.UserDAOImpl;
import org.example.token.ExtendedUsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    private EntityManager entityManager;

    public CustomAuthenticationProvider(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDAO userDAO = new UserDAOImpl(entityManager);
        try {
            User user = userDAO.getUserByUsernamePassword(username, password);
            List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();

            for (Role role : user.getRoles()) {
                grantedAuthorityList.add(new SimpleGrantedAuthority(role.getRole()));
            }

            ExtendedUsernamePasswordAuthenticationToken token = new ExtendedUsernamePasswordAuthenticationToken(username, password, grantedAuthorityList);
            token.setUser(user);

            return token;
        } catch (NoResultException nre) {
            throw new BadCredentialsException("Invalid Credentials provided.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}