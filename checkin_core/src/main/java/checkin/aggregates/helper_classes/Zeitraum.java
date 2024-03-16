package checkin.aggregates.helper_classes;

import java.time.LocalTime;
import java.util.Objects;

public class Zeitraum {

    private LocalTime von;
    private LocalTime bis;

    public Zeitraum(LocalTime von, LocalTime bis) {
        this.von = von;
        this.bis = bis;
    }

    public void setVon(LocalTime von) {
        this.von = von;
    }

    public void setBis(LocalTime bis) {
        this.bis = bis;
    }

    public LocalTime von() {
        return von;
    }

    public LocalTime bis() {
        return bis;
    }

    public int berechneDauer() {
        int vonInMinutes = this.von.getHour() * 60 + this.von.getMinute();
        int bisInMinutes = this.bis.getHour() * 60 + this.bis.getMinute();
        return bisInMinutes - vonInMinutes;
    }

    @Override
    public String toString() {
        return von + " - " + bis + " Uhr";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zeitraum zeitraum = (Zeitraum) o;
        return Objects.equals(von, zeitraum.von) && Objects.equals(bis, zeitraum.bis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(von, bis);
    }
}