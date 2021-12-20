package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "compre_buzz")
@Entity
public class CompreBuzz {

    @Id
    private String href;

    private String program;
    private String startDate;
    private String channel;
    private String days;
    private String post;
    private String comment;
    private String videoView;
    private String news;
    private String video;
    private String family;
    private String individual;
    private String man;
    private String woman;
    private String teenager;
    private String twenties;
    private String thirties;
    private String fourties;
    private String fifties;
    private String sixties;
    private String tvVod;
    private String pcLive;
    private String pcVod;
    private String mobileLive;
    private String mobileVod;

    private String genre;
    private String endDate;
    private String director;
    private String writer;
    private String casts;

    @OneToMany(mappedBy = "compreBuzz", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CompreBuzzMapping> mappings = new ArrayList<CompreBuzzMapping>();

    public void addCompreBuzzMapping(CompreBuzzMapping mapping) {
        this.mappings.add(mapping);
        if (mapping.getCompreBuzz() != this) {
            mapping.setCompreBuzz(this);
        }
    }

    public boolean presentContentIdInMappings(String contentId) {
        for (CompreBuzzMapping mapping : mappings) {
            if (mapping.getContentId().equals(contentId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompreBuzz{" +
                "program='" + program + '\'' +
                ", startDate='" + startDate + '\'' +
                ", channel='" + channel + '\'' +
                ", days='" + days + '\'' +
                ", post='" + post + '\'' +
                ", comment='" + comment + '\'' +
                ", videoView='" + videoView + '\'' +
                ", news='" + news + '\'' +
                ", video='" + video + '\'' +
                ", family='" + family + '\'' +
                ", individual='" + individual + '\'' +
                ", man='" + man + '\'' +
                ", woman='" + woman + '\'' +
                ", teenager='" + teenager + '\'' +
                ", twenties='" + twenties + '\'' +
                ", thirties='" + thirties + '\'' +
                ", fourties='" + fourties + '\'' +
                ", fifties='" + fifties + '\'' +
                ", sixties='" + sixties + '\'' +
                ", tvVod='" + tvVod + '\'' +
                ", pcLive='" + pcLive + '\'' +
                ", pcVod='" + pcVod + '\'' +
                ", mobileLive='" + mobileLive + '\'' +
                ", mobileVod='" + mobileVod + '\'' +
                ", genre='" + genre + '\'' +
                ", endDate='" + endDate + '\'' +
                ", director='" + director + '\'' +
                ", writer='" + writer + '\'' +
                ", casts='" + casts + '\'' +
                ", mappings=" + mappings +
                '}';
    }
}
