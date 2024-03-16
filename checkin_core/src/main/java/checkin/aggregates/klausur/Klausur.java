package checkin.aggregates.klausur;

import checkin.aggregates.helper_classes.Zeitraum;

import java.time.LocalDate;

public record Klausur(Long id,
                      String klausurName,
                      boolean praesenz,
                      LocalDate datum,
                      Zeitraum zeitspanne) {

    public Zeitraum getFreistellung() {
        if (praesenz) {
            return new Zeitraum(zeitspanne().von().minusHours(2), zeitspanne().bis().plusHours(2));
        } else
            return new Zeitraum(zeitspanne().von().minusMinutes(30), zeitspanne().bis());
    }

    @Override
    public String toString() {
        return klausurName + " ("+datum + " - " + zeitspanne +") " + "id= " +id;
    }
}