package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "internet_buzz")
@Entity
public class InternetBuzz {

    @Id
    private String href;

    private String program;
    private String channel;
    private String days;

    private String post;
    private String comment;
    private String videoView;
    private String news;
    private String video;
    private String family;
    private String detail;

    private String genre;
    private String startDate;
    private String endDate;
    private String director;
    private String writer;
    private String casts;

    @OneToMany(mappedBy = "internetBuzz", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InternetBuzzMapping> mappings = new ArrayList<InternetBuzzMapping>();

    public void addInternetBuzzMapping(InternetBuzzMapping mapping) {
        this.mappings.add(mapping);
        if (mapping.getInternetBuzz() != this) {
            mapping.setInternetBuzz(this);
        }
    }

    public boolean presentContentIdInMappings(String contentId) {
        for (InternetBuzzMapping mapping : mappings) {
            if (mapping.getContentId().equals(contentId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "InternetBuzz{" +
                "program='" + program + '\'' +
                ", channel='" + channel + '\'' +
                ", days='" + days + '\'' +
                ", post='" + post + '\'' +
                ", comment='" + comment + '\'' +
                ", videoView='" + videoView + '\'' +
                ", news='" + news + '\'' +
                ", video='" + video + '\'' +
                ", family='" + family + '\'' +
                ", detail='" + detail + '\'' +
                ", genre='" + genre + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", director='" + director + '\'' +
                ", writer='" + writer + '\'' +
                ", casts='" + casts + '\'' +
                ", mappings=" + mappings +
                '}';
    }
}



