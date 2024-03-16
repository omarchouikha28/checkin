package checkin.persistence.aggregates.urlaub.repositories;

import checkin.persistence.aggregates.crudrepos.DBUrlaubRepository;
import checkin.aggregates.urlaub.Urlaub;
import checkin.persistence.aggregates.urlaub.UrlaubDTO;
import checkin.appservices.repositories.UrlaubRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UrlaubRepositoryImpl implements UrlaubRepository {

    private final DBUrlaubRepository repository;

    public UrlaubRepositoryImpl(DBUrlaubRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Urlaub> findAll() {
        List<UrlaubDTO> urlaubDTO = repository.findAll();
        return mapListDtoToAggregate(urlaubDTO);
    }

    @Override
    public Urlaub findUrlaubById(Long id) {
        UrlaubDTO urlaubDTO = repository.findUrlaubDTOById(id);
        return mapDtoToAggregate(urlaubDTO);
    }

    @Override
    public Urlaub findUrlaubByDatum(LocalDate datum) {
        UrlaubDTO urlaubDTO = repository.findUrlaubDTOByDatum(datum);
        return mapDtoToAggregate(urlaubDTO);
    }

    @Override
    public void save(Urlaub urlaub) {
        UrlaubDTO urlaubDTO = new UrlaubDTO(
                urlaub.id(),
                urlaub.datum(),
                urlaub.zeitspanne()
        );
        var savedDTO = repository.save(urlaubDTO);
        urlaub.setId(savedDTO.getId());
    }

    @Override
    public void deleteUrlaubById(Long id) {
        repository.deleteById(id);
    }


    private List<Urlaub> mapListDtoToAggregate(List<UrlaubDTO> urlaubDTOs) {
        List<Urlaub> urlaube = new ArrayList<>();
        for (UrlaubDTO urlaubDTO : urlaubDTOs) {
            urlaube.add(
                    new Urlaub(
                            urlaubDTO.getId(),
                            urlaubDTO.getDatum(),
                            urlaubDTO.getZeitraum()
                    )
            );
        }
        return urlaube;
    }


    private Urlaub mapDtoToAggregate(UrlaubDTO urlaubDTO) {
        return new Urlaub(
                urlaubDTO.getId(),
                urlaubDTO.getDatum(),
                urlaubDTO.getZeitraum()
        );
    }
}
