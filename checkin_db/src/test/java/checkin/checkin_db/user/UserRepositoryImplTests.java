package checkin.checkin_db.user;

import checkin.aggregates.klausur.Klausur;
import checkin.persistence.aggregates.crudrepos.DBKlausurRepository;
import checkin.persistence.aggregates.klausur.repositories.KlausurRepositoryImpl;
import checkin.aggregates.urlaub.Urlaub;
import checkin.persistence.aggregates.crudrepos.DBUrlaubRepository;
import checkin.persistence.aggregates.urlaub.repositories.UrlaubRepositoryImpl;
import checkin.aggregates.user.User;
import checkin.persistence.aggregates.crudrepos.DBUserRepository;
import checkin.persistence.aggregates.user.repositories.UserRepositoryImpl;
import checkin.appservices.configurations.PraktikumConfigurations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mockStatic;


@DataJdbcTest
@ActiveProfiles("database-test")
@Transactional
public class UserRepositoryImplTests {

    @Autowired
    DBUserRepository userRepository;

    @Autowired
    DBKlausurRepository klausurRepository;

    @Autowired
    DBUrlaubRepository urlaubRepository;


    private static PraktikumConfigurations praktikumConfigurations;

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
    @Sql({"classpath:db.migration/V4__userTest.sql"})
    @DisplayName("die richtige Klausuren werden mittels der GithubID angezeigt")
    void test_1() {
        UserRepositoryImpl repo = new UserRepositoryImpl(userRepository);
        List<Klausur> klausuren = repo.findKlausuren("Finalmaestro");
        assertThat(klausuren.size()).isEqualTo(2);
        assertThat(klausuren
                .stream()
                .map(Klausur::klausurName)
                .toArray()).contains("Rechnerarchitektur", "Datenbanken");
    }

    @Test
    @Sql({"classpath:db.migration/V4__userTest.sql"})
    @DisplayName("die richtige Urlaube werden mittels der GithubID angezeigt")
    void test_2() {
        UserRepositoryImpl repo = new UserRepositoryImpl(userRepository);
        List<Urlaub> urlaube = repo.findUrlaube("Finalmaestro");

        assertThat(urlaube.size()).isEqualTo(2);
        assertThat(urlaube
                .stream()
                .map(Urlaub::id)
                .toArray()).contains(4L, 3L);
    }

    @Test
    @Sql({"classpath:db.migration/V4__userTest.sql"})
    @DisplayName("ein User kann erfolgreich gespeichert werden")
    void test_3() {
        UserRepositoryImpl repo = new UserRepositoryImpl(userRepository);
        KlausurRepositoryImpl klausurRepo = new KlausurRepositoryImpl(klausurRepository);
        UrlaubRepositoryImpl urlaubRepo = new UrlaubRepositoryImpl(urlaubRepository);

        User user = new User("omarchouikha28");

        Klausur klausur = klausurRepo.findKlausurById(1L);
        Urlaub urlaub = urlaubRepo.findUrlaubById(1L);

        user.addKlausur(klausur);
        user.addUrlaub(urlaub);

        repo.save(user);

        assertThat(repo.findByGithubId("omarchouikha28")).isEqualTo(user);
    }

}
