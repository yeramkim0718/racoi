package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "related_word")
@Entity
@IdClass(RelatedWordIdentifier.class)

public class RelatedWord {

    @Id
    @Column(length = 100)
    private String channel;
    @Id
    @Column(length = 100)
    private String days;
    @Id
    @Column(length = 100)
    private String priority;
    @Id
    @Column(length = 100)
    private String program;

    private String word;
    private String amount;

    private String href;
    private String genre;
    private String startDate;
    private String endDate;
    private String director;
    private String writer;
    private String casts;

    @OneToMany(mappedBy = "relatedWord", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RelatedWordMapping> mappings = new ArrayList<RelatedWordMapping>();

    public void addRelatedWordMapping(RelatedWordMapping mapping) {
        this.mappings.add(mapping);
        if (mapping.getRelatedWord() != this) {
            mapping.setRelatedWord(this);
        }
    }

    public boolean presentContentIdAndPriorityInMappings(String contentId, String priority) {
        for (RelatedWordMapping mapping : mappings) {
            if (mapping.getContentId().equals(contentId) && mapping.getRelatedWord().getPriority().equals(priority)) {
                return true;
            }
        }
        return false;
    }
}
