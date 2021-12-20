package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"contentId", "contentsetId"})})
@Getter
@Setter
public class InternetBuzzMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String contentId;
    private String contentsetId;

    @ManyToOne
    @JoinColumns({@JoinColumn(name = "href")})
    private InternetBuzz internetBuzz;

    public void setInternetBuzz(InternetBuzz buzz) {
        if (this.internetBuzz != null) {
            this.internetBuzz.getMappings().remove(this);
        }
        this.internetBuzz = buzz;
        if (!buzz.getMappings().contains(this)) {
            buzz.addInternetBuzzMapping(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InternetBuzzMapping internetBuzzMapping = (InternetBuzzMapping) o;
        return Objects.equals(contentId, internetBuzzMapping.getContentId()) &&
                Objects.equals(contentsetId, internetBuzzMapping.getContentsetId());

    }


}