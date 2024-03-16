package checkin.persistence.aggregates.urlaub;

import checkin.aggregates.helper_classes.Zeitraum;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;

import java.time.LocalDate;


public class UrlaubDTO{

    @Id
    private final Long id;

    private final LocalDate datum;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    private final Zeitraum zeitraum;


    public Long getId() {
        return id;
    }


    public LocalDate getDatum() {
        return datum;
    }

    public Zeitraum getZeitraum() {
        return zeitraum;
    }

    public UrlaubDTO(Long id,
                     LocalDate datum,
                     Zeitraum zeitraum) {
        this.id = id;
        this.datum = datum;
        this.zeitraum = zeitraum;
    }

    @Override
    public String toString() {
        return "UrlaubDTO{" +
                "id=" + id +
                ", datum=" + datum +
                ", zeitraum=" + zeitraum +
                '}';
    }



}
