package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.UserCredential;
import bg.mechano.mechano.domain.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link UserCredential} entities.
 *
 * Provides data access operations for authentication credentials,
 * including lookups by provider-specific identifiers and email.
 */
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    Optional<UserCredential> findByEmail(String email);
}