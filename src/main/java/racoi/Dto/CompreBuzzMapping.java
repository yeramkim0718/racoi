package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"contentId", "contentsetId"})})
@Getter
@Setter
public class CompreBuzzMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String contentId;
    private String contentsetId;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "href")})
    private CompreBuzz compreBuzz;

    public void setCompreBuzz(CompreBuzz buzz) {
        if (this.compreBuzz != null) {
            this.compreBuzz.getMappings().remove(this);
        }
        this.compreBuzz = buzz;
        if (!buzz.getMappings().contains(this)) {
            buzz.addCompreBuzzMapping(this);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CompreBuzzMapping compreBuzzMapping = (CompreBuzzMapping) o;
        return Objects.equals(contentId, compreBuzzMapping.getContentId()) &&
                Objects.equals(contentsetId, compreBuzzMapping.getContentsetId());

    }
}