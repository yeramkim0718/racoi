package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import racoi.Dto.InternetBuzz;

@Repository
public interface InternetBuzzRepository extends JpaRepository<InternetBuzz, String> {

    InternetBuzz findFirstByProgramAndChannelAndDays(String program, String channel, String days);
}
