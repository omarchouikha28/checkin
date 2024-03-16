import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.repositories.KlausurRepository;
import checkin.appservices.services.KlausurService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class KlausurServiceTests {

    //Die PraktikumConfigurations müssen manuell in setUp() gesetzt werden.
    private static  PraktikumConfigurations praktikumConfigurations;

    @BeforeAll
    static void setUp() {
        praktikumConfigurations = new PraktikumConfigurations(
                "2022-03-07",
                "2022-03-25",
                "09:30",
                "13:30",
                "240"
        );
    }
    
    @Test
    @DisplayName("Eine noch nicht eingetragene Klausur kann erfolgreich angelegt werden.")
    void test_1() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);

        klausurService.klausurAnlegen(
                228587L,
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        assertThat(klausurService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Eine bereits vorhandene Klausur kann nicht angelegt werden.")
    void test_2() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);
        Klausur klausur = new Klausur(228587L,
                "Mathematik für Informatik 1 - Einzelansicht",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );
        when(klausurRepo.findAll()).thenReturn(List.of(klausur));

        klausurService.klausurAnlegen(
                228587L,
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        List<Exception> fehler = klausurService.getExceptions().stream()
                .filter(e -> e.getMessage().equals("Eine Klausur mit der identischen ID existiert bereits!"))
                .collect(Collectors.toList());

        assertThat(fehler.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Die Klausur-ID einer gueltigen Klausur ist auch im LSF vorhanden")
    void test_3() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);

        Klausur klausur = new Klausur(219960L,
                "Mathematik für Informatik 1",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        klausurService.sucheNachFehlern(klausur);
        List<Exception> fehler = klausurService.getExceptions().stream()
                .filter(e -> e.getMessage().equals("Die von Ihnen angegebene Klausur-ID existiert nicht!"))
                .collect(Collectors.toList());

        assertThat(fehler.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Die Klausur-ID einer nicht gueltigen Klausur ist nicht im LSF vorhanden")
    void test_4() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);

        Klausur klausur = new Klausur(123456L,
                "Mathematik für Informatik 1",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        klausurService.sucheNachFehlern(klausur);
        List<Exception> fehler = klausurService.getExceptions().stream()
                .filter(e -> e.getMessage().equals("Die von Ihnen angegebene Klausur-ID existiert nicht!"))
                .collect(Collectors.toList());

        assertThat(fehler.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Eine Klausur ist auch im LSF als Klausur gekennzeichnet")
    void test_5() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);

        Klausur klausur = new Klausur(229172L,
                "Rechnerarichtektur (3. Klausur)",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        klausurService.sucheNachFehlern(klausur);
        List<Exception> fehler = klausurService.getExceptions().stream()
                .filter(e -> e.getMessage().equals("Der Veranstaltungstyp ist im LSF nicht als 'Klausur' angegeben!"))
                .collect(Collectors.toList());

        assertThat(fehler.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Eine Vorlesung ist im LSF nicht als Klausur eingespeichert")
    void test_6() {
        KlausurRepository klausurRepo = mock(KlausurRepository.class);
        KlausurService klausurService = new KlausurService(klausurRepo);

        Klausur klausur = new Klausur(224708L, // Vorlesung Praesenz Analysis
                "Analysis 1",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        klausurService.sucheNachFehlern(klausur);
        List<Exception> fehler = klausurService.getExceptions().stream()
                .filter(e -> e.getMessage().equals("Der Veranstaltungstyp ist im LSF nicht als 'Klausur' angegeben!"))
                .collect(Collectors.toList());

        assertThat(fehler.isEmpty()).isFalse();
    }
}

