package checkin.aggregates.urlaub;



import checkin.aggregates.helper_classes.Zeitraum;

import java.time.LocalDate;
import java.util.Objects;

public class Urlaub {
    private Long id;
    private LocalDate datum;
    private Zeitraum zeitspanne;

    public Urlaub(Long id, LocalDate datum, Zeitraum zeitspanne) {
        this.id = id;
        this.datum = datum;
        this.zeitspanne = zeitspanne;
    }

    public Long id() {
        return id;
    }

    public LocalDate datum() {
        return datum;
    }

    public Zeitraum zeitspanne() {
        return zeitspanne;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public void setZeitspanne(Zeitraum zeitspanne) {
        this.zeitspanne = zeitspanne;
    }

    @Override
    public String toString() {
        return "datum=" + datum + ", zeitspanne= " + zeitspanne;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Urlaub urlaub = (Urlaub) o;
        return Objects.equals(id, urlaub.id) && Objects.equals(datum, urlaub.datum) && Objects.equals(zeitspanne, urlaub.zeitspanne);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, datum, zeitspanne);
    }
}
