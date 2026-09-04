package bg.mechano.mechano.domain.repository;

import bg.mechano.mechano.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthUserId(Long authUserId);
}