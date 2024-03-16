package checkin.persistence.aggregates;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class KlausurTests {

    @Test
    @DisplayName("Die Freistellung einer Praesenzklausur geht bis 2h vor und nach der Klausur")
    void test_1() {
        Klausur klausur = new Klausur(
                1L,
                "Aldat",
                true,
                LocalDate.of(2022, 03, 20),
                new Zeitraum(
                        LocalTime.of(8, 30),
                        LocalTime.of(10, 30)
                )
        );

        Zeitraum zeitraum = klausur.getFreistellung();

        assertThat(zeitraum.von()).isEqualTo(LocalTime.of(6, 30));
        assertThat(zeitraum.bis()).isEqualTo(LocalTime.of(12, 30));
    }

    @Test
    @DisplayName("Die Freistellung einer Onlineklausur geht nur bis 30min vor der Klausur")
    void test_2() {
        Klausur klausur = new Klausur(
                1L,
                "Aldat",
                false,
                LocalDate.of(2022, 03, 20),
                new Zeitraum(
                        LocalTime.of(8, 30),
                        LocalTime.of(10, 30)
                )
        );

        Zeitraum zeitraum = klausur.getFreistellung();

        assertThat(zeitraum.von()).isEqualTo(LocalTime.of(8, 0));
        assertThat(zeitraum.bis()).isEqualTo(LocalTime.of(10, 30));
    }
}
