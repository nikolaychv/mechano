package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link UserConsent} entities.
 *
 * Provides data access operations for tracking and retrieving
 * user consent records within the platform.
 */
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    List<UserConsent> findByUserId(Long userId);

    List<UserConsent> findByUserIdAndType(Long userId, String type);
}
