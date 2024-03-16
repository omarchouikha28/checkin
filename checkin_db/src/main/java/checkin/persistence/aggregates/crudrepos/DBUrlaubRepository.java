package checkin.persistence.aggregates.crudrepos;

import checkin.persistence.aggregates.urlaub.UrlaubDTO;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface DBUrlaubRepository extends CrudRepository<UrlaubDTO, Long> {
    List<UrlaubDTO> findAll();

    UrlaubDTO findUrlaubDTOById(Long id);

    UrlaubDTO findUrlaubDTOByDatum(LocalDate datum);

    void deleteById(Long id);

}
