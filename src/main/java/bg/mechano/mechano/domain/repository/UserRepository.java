package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 *
 * Provides data access operations for user accounts,
 * including lookups by email and username.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}
