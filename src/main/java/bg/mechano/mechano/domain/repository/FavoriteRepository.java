package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.Favorite;
import bg.mechano.mechano.domain.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link Favorite} entities.
 *
 * Provides data access operations for user favorite services,
 * identified by a composite primary key.
 */
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    List<Favorite> findByIdUserId(Long userId);

    List<Favorite> findByIdRepairShopId(Long repairShopId);
}
