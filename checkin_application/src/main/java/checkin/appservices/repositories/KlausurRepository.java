package checkin.appservices.repositories;

import checkin.aggregates.klausur.Klausur;

import java.util.List;

public interface KlausurRepository {
    List<Klausur> findAll();

    Klausur findKlausurById(Long id);

    Klausur findKlausurByName(String name);

    void save(Klausur klausur);

    void deleteKlausurById(Long id);
}
