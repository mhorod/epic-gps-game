package soturi.server.database;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FightRepository extends JpaRepository<FightEntity, Integer> {
    List<FightEntity> findAllByOrderByIdDesc(Pageable pageable);
}
