package io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.services;

import io.github.rafaviv.yakubackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.yakubackend.iam.domain.services.UserQueryService;
import io.github.rafaviv.yakubackend.iam.domain.model.queries.GetUserByUsernameQuery;
import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import io.github.rafaviv.yakubackend.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import io.github.rafaviv.yakubackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User Details Service Implementation
 * <p>
 * This service is responsible for loading user details from the database
 * for Spring Security authentication.
 * </p>
 */
@Service(value = "defaultUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsServiceExtension {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;

    public UserDetailsServiceImpl(UserRepository userRepository, UserQueryService userQueryService) {
        this.userRepository = userRepository;
        this.userQueryService = userQueryService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userQueryService.handle(new GetUserByUsernameQuery(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsImpl.build(user);
    }

    @Override
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

        return UserDetailsImpl.build(user);
    }
}
