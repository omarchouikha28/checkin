package checkin.checkin_db.urlaub;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.urlaub.Urlaub;
import checkin.persistence.aggregates.crudrepos.DBUrlaubRepository;
import checkin.persistence.aggregates.urlaub.repositories.UrlaubRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJdbcTest
@ActiveProfiles("database-test")
@Transactional
public class UrlaubRepositoryImplTests {

    @Autowired
    DBUrlaubRepository repository;

    @Test
    @Sql({"classpath:db.migration/V3__urlaubTest.sql"})
    @DisplayName("die richtige Anzahl an Urlauben wird zurueckgegeben")
    void test_1() {
        UrlaubRepositoryImpl repo = new UrlaubRepositoryImpl(repository);
        List<Urlaub> urlaube = repo.findAll();
        assertThat(urlaube.size()).isEqualTo(4);
    }

    @Test
    @Sql({"classpath:db.migration/V3__urlaubTest.sql"})
    @DisplayName("Der richtige Urlaub wird mittels der ID zurueckgegeben")
    void test_2() {
        UrlaubRepositoryImpl repo = new UrlaubRepositoryImpl(repository);
        Urlaub urlaub = repo.findUrlaubById(2L);
        assertThat(urlaub.datum()).isEqualTo(LocalDate.of(2022, 3, 15));
        assertThat(urlaub.zeitspanne().von()).isEqualTo(LocalTime.of(10, 45));
        assertThat(urlaub.zeitspanne().bis()).isEqualTo(LocalTime.of(11, 45));
    }

    @Test
    @Sql({"classpath:db.migration/V3__urlaubTest.sql"})
    @DisplayName("Ein Urlaub wird erfolgreich gespeichert")
    void test_3() {
        UrlaubRepositoryImpl repo = new UrlaubRepositoryImpl(repository);
        Urlaub urlaub = new Urlaub(null, LocalDate.of(2022,9,12),
                new Zeitraum(LocalTime.of(10,15), LocalTime.of(11,30)));
        repo.save(urlaub);
        assertThat(repo.findAll().contains(urlaub)).isTrue();
    }

    @Test
    @Sql({"classpath:db.migration/V3__urlaubTest.sql"})
    @DisplayName("ein Urlaub wird erfolgreich geloescht")
    void test_4() {
        UrlaubRepositoryImpl repo = new UrlaubRepositoryImpl(repository);
        repo.deleteUrlaubById(1L);
        assertThat(repo.findAll().size()).isEqualTo(3);
    }
}
