import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;
import checkin.appservices.repositories.UrlaubRepository;
import checkin.appservices.repositories.UserRepository;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.services.UrlaubService;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class UrlaubServiceTests {

    static LocalDate GESTERN;
    static LocalDate MORGEN;
    static LocalDate HEUTE;
    static LocalTime JETZT;


    //Die PraktikumConfigurations müssen manuell in setUp() gesetzt werden.
    private static PraktikumConfigurations praktikumConfigurations;

    /*
    Fixiert das LocalDateTime fürs Testing
    - LocalDate.now() = 2022-03-07
    - LocalTime.now() = 09:30
     */
    @BeforeAll
     static void mockInit() {
        Clock clock = Clock.fixed(Instant.parse("2022-03-07T09:30:00.00Z"), ZoneId.of("UTC"));
        LocalDateTime dateTime = LocalDateTime.now(clock);
        mockStatic(LocalDateTime.class);
        when(LocalDateTime.now()).thenReturn(dateTime);
    }

    @BeforeAll
    static void setUp() {
        GESTERN = LocalDateTime.now().toLocalDate().minusDays(1); // 06.03.2022
        HEUTE = LocalDateTime.now().toLocalDate(); // 07.03.2022
        MORGEN = LocalDateTime.now().toLocalDate().plusDays(1); // 08.03.2022
        JETZT = LocalDateTime.now().toLocalTime(); // 9:30

        praktikumConfigurations = new PraktikumConfigurations(
                "2022-03-07",
                "2022-03-25",
                "09:30",
                "13:30",
                "240"
        );
    }

    @Test
    @DisplayName("Ein Urlaub am Sonntag den 20.03.2022 liegt am Wochenende")
    void test_1() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                HEUTE.with(DayOfWeek.SUNDAY),
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );

        Exception exception = new Exception("Tag liegt am Wochenende");
        urlaubService.anmelden(urlaub, "");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub am Montag liegt nicht am Wochenende")
    void test_2() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                HEUTE.with(DayOfWeek.MONDAY),
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );

        User user = new User("blabla");

        Exception exception = new Exception("Tag liegt am Wochenende");

        when(userRepo.findByGithubId("")).thenReturn(user);

        urlaubService.anmelden(urlaub, "");


        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isFalse();
    }


    @Test
    @DisplayName("Ein Urlaub liegt in der Vergangenheit, wenn dieser gestern war")
    void test_3() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                GESTERN,
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );

        Exception exception = new Exception("Urlaubszeitpunkt liegt in Vergangenheit");
        urlaubService.anmelden(urlaub, "");



        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Urlaub liegt in der Vergangenheit, wenn dessen Zeitpunkt heute schon geschehen ist")
    void test_4() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                HEUTE,
                new Zeitraum(JETZT.minusHours(1), JETZT.plusHours(1))
        );

        Exception exception = new Exception("Urlaubszeitpunkt liegt in Vergangenheit");
        urlaubService.anmelden(urlaub, "");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub liegt in einer falschen Uhrzeit, wenn dieser vor Beginn anfaengt")
    void test_5() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        Urlaub urlaub = new Urlaub(1L,
                HEUTE,
                new Zeitraum(JETZT.minusHours(1), JETZT)
        );

        Exception exception = new Exception("Die frueheste erlaubte Startzeit fuer Sie ist "
                + praktikumConfigurations.getStartTime());
        urlaubService.anmelden(urlaub, "");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub liegt in einer falschen Uhrzeit, wenn dieser ueber das Schichtende hinausgeht")
    void test_6() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        Urlaub urlaub = new Urlaub(1L,
                HEUTE,
                new Zeitraum(JETZT, praktikumConfigurations.getEndTime().plusHours(1))
        );

        Exception exception = new Exception("Die spaeteste erlaubte Endzeit fuer Sie ist "
                + praktikumConfigurations.getEndTime());
        urlaubService.anmelden(urlaub, "");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub von 9:30 bis 10:30 Uhr liegt in einer gueltigen Uhrzeit")
    void test_7() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                HEUTE,
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );
        User user = new User("lahbib");
        Exception exception = new Exception("Zeiten liegen ausserhalb der Praktikumszeit");

        when(userRepo.findByGithubId("lahbib")).thenReturn(user);

        urlaubService.anmelden(urlaub, "lahbib");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isFalse();
    }

    @Test
    @DisplayName("Ein Urlaub am 17.04.2022 liegt nach dem Praktikum")
    void test_8() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        Urlaub urlaub = new Urlaub(1L,
                HEUTE.plusMonths(1),
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );

        Exception exception =
                new Exception("Das Datum ist nach dem letzten Praktikumstag am "
                        + praktikumConfigurations.getEndDate());

        urlaubService.anmelden(urlaub, "");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub am letzten Tag liegt noch im Praktikum")
    void test_9() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getEndDate(),
                new Zeitraum(JETZT, JETZT.plusHours(1))
        );

        User user = new User("ezdin");
        Exception exception =
                new Exception("Das Datum ist nach dem letzten Praktikumstag am "
                        + praktikumConfigurations.getEndDate());

        when(userRepo.findByGithubId("ezdin")).thenReturn(user);

        urlaubService.anmelden(urlaub, "ezdin");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isFalse();
    }

    @Test
    @DisplayName("Ein Urlaub kann storniert werden")
    void test_10() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        urlaubService.stornieren(1L, "");
        verify(urlaubRepo, times(1)).deleteUrlaubById(1L);
    }

    @Test
    @DisplayName("Ein Urlaub kann hinzugefuegt werden, " +
            "wenn er zwischen zwei richtigen Urlaubsbloecken liegt und mit einem davon ueberlappt")
    void test_11() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );
        Urlaub urlaub2 = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(30),
                        praktikumConfigurations.getEndTime())
        );
        Urlaub toBeSaved = new Urlaub(3L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusMinutes(15),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube("temseh")).thenReturn(List.of(urlaub1, urlaub2));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(any());
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub, der mit dem einzigen Block ueberlappt, kann hinzugefuegt werden")
    void test_12() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusMinutes(15),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube("temseh")).thenReturn(List.of(urlaub1));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(any());
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("Ein Block," +
            " der nicht am Ende liegt und der nicht mit dem gespeicherten Urlaubsblock ueberlappt," +
            " kann nicht hinzugefuegt werden")
    void test_13() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusMinutes(30),
                        praktikumConfigurations.getStartTime().plusMinutes(45))
        );

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub1));
        urlaubService.anmelden(toBeSaved, "");

        Exception exception =
                new Exception("Die Urlaubsbloecke muessen am Anfang und am Ende liegen");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein dritter Block kann nicht hinzugefuegt werden")
    void test_14() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );

        Urlaub urlaub2 = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(30),
                        praktikumConfigurations.getEndTime())
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusMinutes(30),
                        praktikumConfigurations.getStartTime().plusMinutes(45))
        );

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub1, urlaub2));
        urlaubService.anmelden(toBeSaved, "");


        Exception exception =
                new Exception("Die Urlaubsbloecke muessen am Anfang und am Ende liegen");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }

    @Test
    @DisplayName("Ein 2. Block kann am Ende hinzufuegefuegt werden")
    void test_15() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(15),
                        praktikumConfigurations.getEndTime())
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub1));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(toBeSaved);
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();

    }

    @Test
    @DisplayName("Ein 2. Block kann am Anfang hinzufuegefuegt werden")
    void test_16() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(30),
                        praktikumConfigurations.getEndTime())
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(15))
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(toBeSaved);
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Überlappung mit einem Block, aber keine genügend Zeit")
    void test_17() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusMinutes(30),
                        praktikumConfigurations.getStartTime().plusHours(3))
        );

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub));
        urlaubService.anmelden(toBeSaved, "");

        Exception exception =
                new Exception("Sie koennen entweder den gesamten Tag frei nehmen, oder bis zu 2,5 Stunden");


        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();

    }

    @Test
    @DisplayName("Ein Urlaub, der mit dem Block am Ende überlappt und alle Bedingungen erfüllt," +
            " kann hinzugefuegt werden")
    void test_18() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        Urlaub urlaub2 = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(15),
                        praktikumConfigurations.getEndTime())
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getEndTime().minusMinutes(30),
                        praktikumConfigurations.getEndTime())
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube("temseh")).thenReturn(List.of(urlaub1, urlaub2));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");


        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(any());
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Ein Urlaub der schon existiert kann nicht noch mal hinzugefeugt werden")
    void test_19() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        when(userRepo.findUrlaube(any())).thenReturn(List.of(urlaub1));
        urlaubService.anmelden(toBeSaved, "");

        verify(urlaubRepo, times(0)).save(toBeSaved);
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Ueberlappung nach rechts")
    void test_20() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub urlaub1 = new Urlaub(1L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime().plusHours(1),
                        praktikumConfigurations.getStartTime().plusMinutes(90))
        );

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(75))
        );


        User user = new User("temseh");

        when(userRepo.findUrlaube("temseh")).thenReturn(List.of(urlaub1));
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(any());
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }


    @Test
    @DisplayName("Ein erster Urlaub hinzugefeugen")
    void test_21() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusMinutes(30))
        );

        User user = new User("temseh");

        when(userRepo.findUrlaube(any())).thenReturn(Collections.emptyList());
        when(userRepo.findByGithubId("temseh")).thenReturn(user);

        urlaubService.anmelden(toBeSaved, "temseh");

        verify(userRepo,times(1)).update(user);
        verify(urlaubRepo,times(1)).save(toBeSaved);
        assertThat(urlaubService.getExceptions().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Ein erster Urlaub kann nicht hinzugefeugt werden, wenn er die Bedingungen nicht erfuellt")
    void test_22() {
        UserRepository userRepo = mock(UserRepository.class);
        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);

        Urlaub toBeSaved = new Urlaub(2L,
                praktikumConfigurations.getStartDate(),
                new Zeitraum(praktikumConfigurations.getStartTime(),
                        praktikumConfigurations.getStartTime().plusHours(3))
        );

        when(userRepo.findUrlaube(any())).thenReturn(Collections.emptyList());
        urlaubService.anmelden(toBeSaved, "");

        Exception exception =
                new Exception("Sie koennen entweder den gesamten Tag frei nehmen, oder bis zu 2,5 Stunden");

        assertThat(
                urlaubService.getExceptions()
                        .stream()
                        .map(Throwable::getMessage)
                        .collect(Collectors.toList())
                        .contains(exception.getMessage())
        ).isTrue();
    }
//    @Test
//    @DisplayName("Ein Urlaub kann aufgrund zu wenig Resturlaub nicht angemeldet werden")
//    void test_23() {
//        UserRepository userRepo = mock(UserRepository.class);
//        UrlaubRepository urlaubRepo = mock(UrlaubRepository.class);
//        UrlaubService urlaubService = new UrlaubService(urlaubRepo, userRepo, praktikumConfigurations);
//
//        Urlaub alreadySaved = new Urlaub(2L,
//                praktikumConfigurations.getEndDate(),
//                new Zeitraum(praktikumConfigurations.getStartTime(),
//                        praktikumConfigurations.getStartTime().plusHours(4))
//        );
//        Urlaub toBeSaved = new Urlaub(2L,
//                praktikumConfigurations.getEndDate().minusDays(1),
//                new Zeitraum(praktikumConfigurations.getStartTime(),
//                        praktikumConfigurations.getStartTime().plusHours(1))
//        );
//
//        when(userRepo.findUrlaube(any())).thenReturn(List.of(alreadySaved));
//        urlaubService.anmelden(toBeSaved, "");
//
//        Exception exception =
//                new Exception("Sie haben nicht mehr so viel Resturlaub uebrig");
//
//        assertThat(
//                urlaubService.getExceptions()
//                        .stream()
//                        .map(Throwable::getMessage)
//                        .collect(Collectors.toList())
//                        .contains(exception.getMessage())
//        ).isTrue();
//    }

}
