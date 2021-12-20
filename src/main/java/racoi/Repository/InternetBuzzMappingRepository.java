package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import racoi.Dto.InternetBuzz;
import racoi.Dto.InternetBuzzMapping;


@Repository
public interface InternetBuzzMappingRepository extends JpaRepository<InternetBuzzMapping, Long> {

    InternetBuzzMapping findFistByContentIdAndContentsetId(String contentId, String contentSetId);

}
