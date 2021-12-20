package racoi.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import racoi.Dto.RelatedWord;
import racoi.Dto.RelatedWordIdentifier;

import java.util.List;

public interface RelatedWordRepository extends JpaRepository<RelatedWord, RelatedWordIdentifier> {
}
