package checkin.persistence.aggregates.crudrepos;

import checkin.persistence.aggregates.klausur.KlausurDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DBKlausurRepository extends CrudRepository<KlausurDTO, Long> {


    List<KlausurDTO> findAll();

    KlausurDTO findKlausurDTOById(Long id);

    @Query("SELECT * FROM klausur_dto WHERE klausur_name LIKE :klausurName")
    KlausurDTO findKlausurDTOByKlausurName(@Param("klausurName") String klausurName);

    void deleteById(Long id);
}
