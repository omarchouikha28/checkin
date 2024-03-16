package checkin.checkin_db.klausur;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.persistence.aggregates.crudrepos.DBKlausurRepository;
import checkin.persistence.aggregates.klausur.repositories.KlausurRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class KlausurRepositoryImplTests {

    @Autowired
    DBKlausurRepository repository;

    @Test
    @Sql({"classpath:db.migration/V2__klausurTest.sql"})
    @DisplayName("die richtige Anzahl an Klausuren wird zurueckgegeben")
    void test_1() {
        KlausurRepositoryImpl repo = new KlausurRepositoryImpl(repository);
        List<Klausur> klausuren = repo.findAll();
        assertThat(klausuren.size()).isEqualTo(5);
    }

    @Test
    @Sql({"classpath:db.migration/V2__klausurTest.sql"})
    @DisplayName("die richtige Klausur wird mittels der ID zurueckgegeben")
    void test_2() {
        KlausurRepositoryImpl repo = new KlausurRepositoryImpl(repository);
        Klausur klausur = repo.findKlausurById(2L);
        assertThat(klausur.klausurName()).isEqualTo("Propra");
    }

    @Test
    @Sql({"classpath:db.migration/V2__klausurTest.sql"})
    @DisplayName("die richtige Klausur wird mittels des Namens zurueckgegeben")
    void test_3() {
        KlausurRepositoryImpl repo = new KlausurRepositoryImpl(repository);
        Klausur klausur = repo.findKlausurByName("Rechnerarchitektur");
        assertThat(klausur.id()).isEqualTo(1);
    }

    @Test
    @Sql({"classpath:db.migration/V2__klausurTest.sql"})
    @DisplayName("eine Klausur wird erfolgreich gespeichert")
    void test_4() {
        KlausurRepositoryImpl repo = new KlausurRepositoryImpl(repository);
        Klausur klausur = new Klausur(6L,"Analysis",true, LocalDate.of(2022,9,12),
                new Zeitraum(LocalTime.of(10,15), LocalTime.of(11,30)));
        repo.save(klausur);
        assertThat(repo.findKlausurById(6L)).isEqualTo(klausur);
    }

    @Test
    @Sql({"classpath:db.migration/V2__klausurTest.sql"})
    @DisplayName("eine Klausur wird erfolgreich geloescht")
    void test_5() {
        KlausurRepositoryImpl repo = new KlausurRepositoryImpl(repository);
        repo.deleteKlausurById(1L);

        assertThat(repo.findAll().size()).isEqualTo(4);
    }

}