package com.multi.tracklearn.auth;

import com.multi.tracklearn.domain.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class JwtUserAuthentication extends AbstractAuthenticationToken {

    private final User user;

    public JwtUserAuthentication(User user) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.user = user;
        setAuthenticated(true);
    }


    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}
