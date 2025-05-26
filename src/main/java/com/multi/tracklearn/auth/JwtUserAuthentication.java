package com.multi.tracklearn.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class JwtUserAuthentication extends AbstractAuthenticationToken {

    private final String email;

    public JwtUserAuthentication(String email) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        this.email = email;
        setAuthenticated(true);
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

}
