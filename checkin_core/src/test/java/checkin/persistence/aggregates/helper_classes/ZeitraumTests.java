package checkin.persistence.aggregates.helper_classes;

import checkin.aggregates.helper_classes.Zeitraum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ZeitraumTests {

    @Test
    @DisplayName("Die Dauer des Zeitraums von 9:30 bis 10:45 Uhr ist 75min")
    void test_1(){
        LocalTime startpunkt = LocalTime.of(9, 30);
        LocalTime endpunkt = LocalTime.of(10, 45);

        Zeitraum zeitraum = new Zeitraum(startpunkt, endpunkt);

        assertThat(zeitraum.berechneDauer()).isEqualTo(75);
    }
}
