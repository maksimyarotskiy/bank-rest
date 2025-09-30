package com.bank.bank_rest.model.enums;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN {
        @Override
        public Set<GrantedAuthority> getAuthorities() {
            return Set.of("ADMIN", "USER").stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
    },
    USER {
        @Override
        public Set<GrantedAuthority> getAuthorities() {
            return Set.of("USER").stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
    };
    
    public abstract Set<GrantedAuthority> getAuthorities();
}
