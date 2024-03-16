package checkin.persistence.aggregates.klausur.repositories;

import checkin.persistence.aggregates.crudrepos.DBKlausurRepository;
import checkin.aggregates.klausur.Klausur;
import checkin.persistence.aggregates.klausur.KlausurDTO;
import checkin.appservices.repositories.KlausurRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class KlausurRepositoryImpl implements KlausurRepository {

    private final DBKlausurRepository repository;

    public KlausurRepositoryImpl(DBKlausurRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Klausur> findAll() {
        List<KlausurDTO> klausurDTO = repository.findAll();
        return mapListDtoToAggregate(klausurDTO);
    }

    @Override
    public Klausur findKlausurById(Long id) {
        KlausurDTO klausurDTO = repository.findKlausurDTOById(id);
        return new Klausur(
                klausurDTO.getId(),
                klausurDTO.getKlausurName(),
                klausurDTO.isPraesenz(),
                klausurDTO.getDatum(),
                klausurDTO.getZeitspanne()
        );
    }


    @Override
    public Klausur findKlausurByName(String name) {
        KlausurDTO klausurDTO = repository.findKlausurDTOByKlausurName(name);
        return new Klausur(
                klausurDTO.getId(),
                klausurDTO.getKlausurName(),
                klausurDTO.isPraesenz(),
                klausurDTO.getDatum(),
                klausurDTO.getZeitspanne()
        );
    }

    @Override
    public void save(Klausur klausur) {
        KlausurDTO klausurDTO = new KlausurDTO(
                klausur.id(),
                klausur.klausurName(),
                klausur.praesenz(),
                klausur.datum(),
                klausur.zeitspanne(),
                true
        );
        repository.save(klausurDTO);
    }

    @Override
    public void deleteKlausurById(Long id) {
        repository.deleteById(id);
    }



    private List<Klausur> mapListDtoToAggregate(List<KlausurDTO> klausurDTOs) {
        List<Klausur> klausuren = new ArrayList<>();
        for (KlausurDTO klausurDTO : klausurDTOs) {
            klausuren.add(
                    new Klausur(
                            klausurDTO.getId(),
                            klausurDTO.getKlausurName(),
                            klausurDTO.isPraesenz(),
                            klausurDTO.getDatum(),
                            klausurDTO.getZeitspanne()
                    )
            );
        }
        return klausuren;
    }
}
