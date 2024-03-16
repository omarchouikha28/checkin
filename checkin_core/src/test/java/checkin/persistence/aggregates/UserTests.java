package checkin.persistence.aggregates;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.klausur.KlausurRef;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.urlaub.UrlaubRef;
import checkin.aggregates.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserTests {

    @Test
    @DisplayName("Ein Urlaub wird hinzugefuegt und der Resturlaub wird aktualisiert")
    void test_1(){
        Urlaub urlaub = new Urlaub(1L,
                LocalDate.of(2022, 03, 14),
                new Zeitraum(
                        LocalTime.of(9, 30),
                        LocalTime.of(10, 0)
                )
        );
        UrlaubRef urlaubRef = new UrlaubRef(urlaub.id());
        User user = new User("DenKue");

        user.addUrlaub(urlaub);

        assertThat(user.getUrlaube().contains(urlaubRef)).isTrue();
        assertThat(user.getResturlaub()).isEqualTo(210);
        assertThat(user.getSummeUrlaube()).isEqualTo(30);
    }

    @Test
    @DisplayName("Der Resturlaub wird korrekt ermittelt")
    void test_2(){
        Urlaub urlaub = new Urlaub(1L,
                LocalDate.of(2022, 03, 14),
                new Zeitraum(
                        LocalTime.of(11, 30),
                        LocalTime.of(12, 0)
                )
        );
        User user = new User("DenKue");

        user.addUrlaub(urlaub);

        assertThat(user.getResturlaub()).isEqualTo(210);
    }

    @Test
    @DisplayName("Die Summe aller Urlaube wird korrekt ermittelt")
    void test_3(){
        Urlaub urlaub1 = new Urlaub(1L,
                LocalDate.of(2022, 03, 14),
                new Zeitraum(
                        LocalTime.of(11, 30),
                        LocalTime.of(12, 0)
                )
        );
        Urlaub urlaub2 = new Urlaub(2L,
                LocalDate.of(2022, 03, 17),
                new Zeitraum(
                        LocalTime.of(9, 30),
                        LocalTime.of(12, 0)
                )
        );
        User user = new User("DenKue");

        user.addUrlaub(urlaub1);
        user.addUrlaub(urlaub2);

        assertThat(user.getSummeUrlaube()).isEqualTo(180);
    }

    @Test
    @DisplayName("Ein Urlaub wird erfolgreich geloescht")
    void test_4(){
        Urlaub urlaub1 = new Urlaub(1L,
                LocalDate.of(2022, 03, 14),
                new Zeitraum(
                        LocalTime.of(11, 30),
                        LocalTime.of(12, 0)
                )
        );
        Urlaub urlaub2 = new Urlaub(2L,
                LocalDate.of(2022, 03, 17),
                new Zeitraum(
                        LocalTime.of(9, 30),
                        LocalTime.of(12, 0)
                )
        );
        UrlaubRef urlaubRef = new UrlaubRef(urlaub2.id());
        User user = new User("DenKue");

        user.addUrlaub(urlaub1);
        user.addUrlaub(urlaub2);
        user.deleteUrlaub(urlaub1);

        assertThat(user.getUrlaube().contains(urlaubRef)).isTrue();
        assertThat(user.getResturlaub()).isEqualTo(90);
        assertThat(user.getSummeUrlaube()).isEqualTo(150);
    }

    @Test
    @DisplayName("Zwei Klausuren werden erfolgreich hinzugefuegt")
    void test_5(){
        Klausur klausur1 = new Klausur(
                1L,
                "Aldat",
                true,
                LocalDate.of(2022, 03, 20),
                new Zeitraum(
                        LocalTime.of(8, 30),
                        LocalTime.of(10, 30)
                )
        );
        Klausur klausur2 = new Klausur(
                2L,
                "Rechnerarchitektur",
                false,
                LocalDate.of(2022, 03, 23),
                new Zeitraum(
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 30)
                )
        );
        KlausurRef klausurRef1 = new KlausurRef(klausur1.id());
        KlausurRef klausurRef2 = new KlausurRef(klausur2.id());
        User user = new User("DenKue");

        user.addKlausur(klausur1);
        user.addKlausur(klausur2);

        assertThat(user.getKlausuren().contains(klausurRef1)).isTrue();
        assertThat(user.getKlausuren().contains(klausurRef2)).isTrue();
        assertThat(user.getKlausuren().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Eine Klausur wird erfolgreich storniert")
    void test_6(){
        Klausur klausur1 = new Klausur(
                1L,
                "Aldat",
                true,
                LocalDate.of(2022, 03, 20),
                new Zeitraum(
                        LocalTime.of(8, 30),
                        LocalTime.of(10, 30)
                )
        );
        Klausur klausur2 = new Klausur(
                2L,
                "Rechnerarchitektur",
                false,
                LocalDate.of(2022, 03, 23),
                new Zeitraum(
                        LocalTime.of(10, 30),
                        LocalTime.of(11, 30)
                )
        );
        KlausurRef klausurRef1 = new KlausurRef(klausur1.id());
        KlausurRef klausurRef2 = new KlausurRef(klausur2.id());
        User user = new User("DenKue");

        user.addKlausur(klausur1);
        user.addKlausur(klausur2);
        user.deleteKlausur(klausur1);

        assertThat(user.getKlausuren().contains(klausurRef1)).isFalse();
        assertThat(user.getKlausuren().contains(klausurRef2)).isTrue();
        assertThat(user.getKlausuren().size()).isEqualTo(1);
    }
}
