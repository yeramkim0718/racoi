package racoi.Dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class RelatedWordIdentifier implements Serializable {
    private String channel;
    private String days;
    private String priority;
    private String program;
    private String wordClass;

    public RelatedWordIdentifier() {

    }

    public RelatedWordIdentifier(String channel, String days, String priority, String program, String wordClass) {
        this.priority = priority;
        this.program = program;
        this.channel = channel;
        this.days = days;
        this.wordClass = wordClass;

    }

    @Override
    public int hashCode() {
        String identifier = channel.concat(days).concat(priority).concat(program).concat(wordClass);
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RelatedWordIdentifier relatedWordIdentifier = (RelatedWordIdentifier) o;
        return Objects.equals(priority, relatedWordIdentifier.getPriority()) &&
                Objects.equals(channel, relatedWordIdentifier.getChannel()) &&
                Objects.equals(days, relatedWordIdentifier.getDays()) &&
                Objects.equals(program, relatedWordIdentifier.getProgram()) &&
                Objects.equals(wordClass, relatedWordIdentifier.getWordClass());

    }
}
