package io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface UserDetailsServiceExtension extends UserDetailsService {

    UserDetails loadUserById(Long userId) throws UsernameNotFoundException;

}
