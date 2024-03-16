package checkin.persistence.aggregates.klausur;

import checkin.aggregates.helper_classes.Zeitraum;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Embedded;

import java.time.LocalDate;


public class KlausurDTO implements Persistable<Long>{

    @Id
    private final Long id;
    private final String klausurName;
    private final boolean praesenz;
    private final LocalDate datum;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    private final Zeitraum zeitspanne;

    @Transient
    private final boolean isNew;

    public String getKlausurName() {
        return klausurName;
    }

    public boolean isPraesenz() {
        return praesenz;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public Zeitraum getZeitspanne() {
        return zeitspanne;
    }

    public KlausurDTO(Long id,
                      String klausurName,
                      boolean praesenz,
                      LocalDate datum,
                      Zeitraum zeitspanne,
                      boolean isNew) {
        this.id = id;
        this.klausurName = klausurName;
        this.praesenz = praesenz;
        this.datum = datum;
        this.zeitspanne = zeitspanne;
        this.isNew = isNew;
    }

    @PersistenceConstructor
    public KlausurDTO(Long id,
                      String klausurName,
                      boolean praesenz,
                      LocalDate datum,
                      Zeitraum zeitspanne) {
        this.id = id;
        this.klausurName = klausurName;
        this.praesenz = praesenz;
        this.datum = datum;
        this.zeitspanne = zeitspanne;
        this.isNew = false;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
