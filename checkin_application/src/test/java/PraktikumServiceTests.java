import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;
import checkin.appservices.repositories.UrlaubRepository;
import checkin.appservices.repositories.UserRepository;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.services.UrlaubService;
import checkin.appservices.services.PraktikumService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PraktikumServiceTests {

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
    @DisplayName("Eine Klausur kann hinzugefuegt werden")
    void test_1() {
        UrlaubService urlaubService = mock(UrlaubService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PraktikumService service = new PraktikumService(urlaubService, userRepository, praktikumConfigurations);

        User user = new User("amrouch");

        Klausur klausur = new Klausur(2L,
                "rechner",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        when(userRepository.findByGithubId("amrouch")).thenReturn(user);
        service.fuerKlausurAnmelden(klausur, "amrouch");

        verify(userRepository, times(1)).update(user);
    }

    @Test
    @DisplayName("Ein Urlaub kann hinzugefuegt werden")
    void test_2() {
        UrlaubService urlaubService = mock(UrlaubService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PraktikumService service = new PraktikumService(urlaubService, userRepository, praktikumConfigurations);

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        User user = new User("temseh");

        when(userRepository.findUrlaube("temseh")).thenReturn(Collections.emptyList());
        when(userRepository.findByGithubId("temseh")).thenReturn(user);

        service.urlaubBuchen(toBeSaved, "temseh");

        verify(urlaubService, times(1)).anmelden(toBeSaved, "temseh");
        assertThat(service.getUrlaubExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub kann storniert werden")
    void test_3() {
        UrlaubService urlaubService = mock(UrlaubService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PraktikumService service = new PraktikumService(urlaubService, userRepository, praktikumConfigurations);

        Urlaub toBeDeleted = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );
        User user = new User("temseh");
        user.addUrlaub(toBeDeleted);

        when(userRepository.findUrlaube("temseh")).thenReturn(List.of(toBeDeleted));
        when(userRepository.findByGithubId("temseh")).thenReturn(user);

        service.urlaubStornieren(toBeDeleted, "temseh");


        verify(userRepository, times(1)).update(user);
        assertThat(service.getUser("temseh").getUrlaube().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Eine Klausur kann storniert werden")
    void test_4() {
        UrlaubService urlaubService = mock(UrlaubService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PraktikumService service = new PraktikumService(urlaubService, userRepository, praktikumConfigurations);

        Klausur klausur = new Klausur(2L,
                "rechner",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        User user = new User("temseh");
        user.addKlausur(klausur);

        when(userRepository.findKlausuren("temseh")).thenReturn(List.of(klausur));
        when(userRepository.findByGithubId("temseh")).thenReturn(user);

        service.klausurStornieren(klausur, "temseh");

        verify(userRepository, times(1)).update(user);
        assertThat(service.getUser("temseh").getKlausuren().isEmpty()).isTrue();
    }

    // Ab hier Tests die Klausuren am Tag haben
    @Test
    @DisplayName("Ein Urlaub wird komplett storniert, wenn man sich fuer eine Klausur anmeldet, welche diesen abdeckt")
    void test_5() {
        UrlaubService urlaubService = mock(UrlaubService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PraktikumService service = new PraktikumService(urlaubService, userRepository, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        Klausur klausur = new Klausur(2L,
                "Rechner",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(2),
                        praktikumConfigurations.getEndTime())
        );

        User user = new User("Finalmaestro");
        user.addUrlaub(urlaub);

        when(userRepository.findByGithubId("Finalmaestro")).thenReturn(user);
        when(userRepository.findUrlaube("Finalmaestro")).thenReturn(List.of(urlaub));

        service.fuerKlausurAnmelden(klausur, "Finalmaestro");

        verify(urlaubService, times(1)).forceUrlaubStornierung(urlaub, user.getGithubID());
        verify(userRepository, times(2)).update(user);
        assertThat(userRepository.findByGithubId("Finalmaestro").getUrlaube().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Bei einer Praesenzklausur ist ein Nutzer 2h vor Klausurbeginn freigestellt," +
            " weswegen alle betroffenen Urlaube angepasst werden")
    void test_6() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime().plusHours(3),
                        praktikumConfigurations.getStartTime().plusHours(5))
        );

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(2))
        );

        Urlaub urlaubNachAnpassung = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(urlaub, "");

        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung);
    }

    @Test
    @DisplayName("Bei einer Praesenzklausur ist ein Nutzer 2h nach Klausurende freigestellt," +
            " weswegen alle betroffenen Urlaube angepasst werden")
    void test_7() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                true,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(4))
        );

        Urlaub urlaubNachAnpassung = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(3),
                        praktikumConfigurations.getStartTime().plusHours(4))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(urlaub, "");

        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung);
    }

    @Test
    @DisplayName("Bei einer Onlineklausur ist ein Nutzer 30min vor Klausurbeginn freigestellt," +
            " weswegen alle betroffenen Urlaube angepasst werden")
    void test_8() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime().plusHours(3),
                        praktikumConfigurations.getStartTime().plusHours(5))
        );

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(1),
                        praktikumConfigurations.getStartTime().plusHours(3))
        );

        Urlaub urlaubNachAnpassung = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(1),
                        praktikumConfigurations.getStartTime().plusHours(2).plusMinutes(30))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(urlaub, "");

        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung);
    }

    @Test
    @DisplayName("Bei einer Onlineklausur ist ein Nutzer 0min nach Klausurende freigestellt," +
            " weswegen alle betroffenen Urlaube angepasst werden")
    void test_9() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(2))
        );

        Urlaub urlaubNachAnpassung = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(1),
                        praktikumConfigurations.getStartTime().plusHours(2))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(urlaub, "");

        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung);
    }

    @Test
    @DisplayName("Ein Urlaub kollidiert mit einer Onlineklausur und wird in zwei Teile geteilt")
    void test_10() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime().plusHours(2), // standard 11:30 Uhr
                        praktikumConfigurations.getStartTime().plusHours(3)) //standard 12:30 Uhr
        );

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(), // standard 9:30 Uhr
                        praktikumConfigurations.getStartTime().plusHours(4)) // standard 13:30 Uhr
        );

        Urlaub urlaubNachAnpassung1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(), // standard 9:30 Uhr
                        praktikumConfigurations.getStartTime().plusHours(1).plusMinutes(30)) // standard 11:00 Uhr
        );

        Urlaub urlaubNachAnpassung2 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(3), // standard 12:30 Uhr
                        praktikumConfigurations.getEndTime()) // standard 13:30
        );

        User user = new User("");
        user.addKlausur(klausur);

        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(urlaub, "");

        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung1);
        verify(urlaubRepo, times(1)).save(urlaubNachAnpassung2);
    }

    @Test
    @DisplayName("Ein eigentlich zu langer Urlaub (3 Stunden) " +
            "kann aufgrund einer Klausur am selben Tag eingefuegt werden")
    void test_11() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime(),
                        praktikumConfigurations.getEndTime().plusMinutes(30))
        );


        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(3))
        );

        Urlaub rightSave = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(
                        praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(3))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(toBeSaved, "");


        verify(urlaubRepo, times(1)).save(rightSave);
    }



    @Test
    @DisplayName("Ein Urlaub wird von zwei Klausuren zerkleinert")
    void test_26() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur1 = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        Klausur klausur2 = new Klausur(214535L,
                "AlDat",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusHours(1),
                        praktikumConfigurations.getEndTime())
        );

        // 9:30 - 13:30
        Urlaub toBeSaved = new Urlaub(3L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(4))
        );

        // 10:30 - 12:00
        Urlaub rightSave = new Urlaub(3L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(1),
                        praktikumConfigurations.getStartTime().plusHours(2).plusMinutes(30))
        );

        User user = new User("");
        user.addKlausur(klausur1);
        user.addKlausur(klausur2);


        List<Klausur> angemeldeteKlausuren = List.of(klausur1,klausur2);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(toBeSaved,"");

        verify(urlaubRepo,times(1)).save(rightSave);
    }

    @Test
    @DisplayName("Urlaub kollidiert mit Klausur und es wird kein Urlaub angelegt")
    void test_27() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        PraktikumService praktikumService = new PraktikumService(urlaubService, userRepo, praktikumConfigurations);

        Klausur klausur1 = new Klausur(219960L,
                "Rechnerarichtektur",
                false,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        // 9:30 - 10:30
        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(1))
        );

        User user = new User("");
        List<Klausur> angemeldeteKlausuren = List.of(klausur1);

        when(userRepo.findByGithubId("")).thenReturn(user);
        when(userRepo.findKlausuren("")).thenReturn(angemeldeteKlausuren);

        praktikumService.urlaubBuchen(toBeSaved,"");
        verify(urlaubRepo,times(0)).save(any());
    }
}