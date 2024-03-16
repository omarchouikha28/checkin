package checkin.appservices.repositories;

import checkin.aggregates.urlaub.Urlaub;

import java.time.LocalDate;
import java.util.List;

public interface UrlaubRepository {

    List<Urlaub> findAll();

    Urlaub findUrlaubById(Long id);

    Urlaub findUrlaubByDatum(LocalDate datum);

    void save(Urlaub urlaub);
    
    void deleteUrlaubById(Long id);


}
