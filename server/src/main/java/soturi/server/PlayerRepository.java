package soturi.server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Integer> {
    Optional<PlayerEntity> findByName(String name);
    boolean existsByName(String name);
}
