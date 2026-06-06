package io.github.rafaviv.yakubackend.iam.infrastructure.hashing.bcrypt;

import io.github.rafaviv.yakubackend.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
