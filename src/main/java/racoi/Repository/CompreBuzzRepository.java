package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import racoi.Dto.CompreBuzz;

@Repository
public interface CompreBuzzRepository extends JpaRepository<CompreBuzz, String> {
}
