package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"contentId", "contentsetId", "priority"})})
public class RelatedWordMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(length = 400)
    private String contentId;
    @Column(length = 400)
    private String contentsetId;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "channel"),
            @JoinColumn(name = "days"),
            @JoinColumn(name = "priority"),
            @JoinColumn(name = "program")})
    private RelatedWord relatedWord = null;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RelatedWordMapping relatedWordMapping = (RelatedWordMapping) o;
        return Objects.equals(contentId, relatedWordMapping.getContentId()) &&
                Objects.equals(contentsetId, relatedWordMapping.getContentsetId()) &&
                Objects.equals(relatedWord.getPriority(), relatedWordMapping.getRelatedWord().getPriority());

    }

}