package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import racoi.Dto.RelatedWordMapping;

@Repository
public interface RelatedWordMappingRepository extends JpaRepository<RelatedWordMapping, Long> {
}
