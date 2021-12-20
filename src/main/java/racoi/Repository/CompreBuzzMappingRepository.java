package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import racoi.Dto.CompreBuzzMapping;

@Repository
public interface CompreBuzzMappingRepository extends JpaRepository<CompreBuzzMapping, Long> {

}
