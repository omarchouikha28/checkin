package checkin.persistence.aggregates.user.repositories;

import checkin.persistence.aggregates.crudrepos.DBUserRepository;
import checkin.aggregates.klausur.Klausur;
import checkin.persistence.aggregates.klausur.KlausurDTO;
import checkin.aggregates.klausur.KlausurRef;
import checkin.persistence.aggregates.klausur.KlausurRefDTO;
import checkin.aggregates.urlaub.Urlaub;
import checkin.persistence.aggregates.urlaub.UrlaubDTO;
import checkin.aggregates.urlaub.UrlaubRef;
import checkin.persistence.aggregates.urlaub.UrlaubRefDTO;
import checkin.aggregates.user.User;
import checkin.persistence.aggregates.user.UserDTO;
import checkin.appservices.repositories.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DBUserRepository repository;

    public UserRepositoryImpl(DBUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User findByGithubId(String githubID) {
        UserDTO userDTO = repository.findUserDTOByGithubID(githubID);
        if (userDTO != null) {
            User user = new User((userDTO.getGithubID()));
            List<Urlaub> alleUrlaube = mapUrlaubDtoToAggregate(repository.findUrlaube(githubID));
            List<Klausur> alleKlausuren = mapKlausurenDtoToAggregate(repository.findKlausuren(githubID));

            for (Urlaub urlaub : alleUrlaube){
                user.addUrlaub(urlaub);
            }
            for (Klausur klausur : alleKlausuren){
                user.addKlausur(klausur);
            }
            return user;
        }
        return null;
    }


    @Override
    public List<Klausur> findKlausuren(String githubID) {
        List<KlausurDTO> klausuren = repository.findKlausuren(githubID);
        return mapKlausurenDtoToAggregate(klausuren);
    }

    @Override
    public List<Urlaub> findUrlaube(String githubID) {
        List<UrlaubDTO> urlaube = repository.findUrlaube(githubID);
        return mapUrlaubDtoToAggregate(urlaube);
    }

    @Override
    public void save(User user) {
        UserDTO userDTO = new UserDTO(
                user.getGithubID(),
                mapUrlaubRefDtoToAggregate(user.getUrlaube()),
                mapKlausurRefDtoToAggregate(user.getKlausuren()),
                true
        );
        repository.save(userDTO);
    }

    @Override
    public void update(User user) {
        UserDTO userDTO = new UserDTO(
                user.getGithubID(),
                mapUrlaubRefDtoToAggregate(user.getUrlaube()),
                mapKlausurRefDtoToAggregate(user.getKlausuren()),
                false
        );
        repository.save(userDTO);
    }

    private List<Klausur> mapKlausurenDtoToAggregate(List<KlausurDTO> klausurDTOS) {
        List<Klausur> klausuren = new ArrayList<>();
        for (KlausurDTO klausurDTO : klausurDTOS) {
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

    private List<Urlaub> mapUrlaubDtoToAggregate(List<UrlaubDTO> urlaubDTOS) {
        List<Urlaub> urlaube = new ArrayList<>();
        for (UrlaubDTO urlaubDTO : urlaubDTOS) {
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

    private Set<UrlaubRefDTO> mapUrlaubRefDtoToAggregate(Set<UrlaubRef> urlaubRefs) {
        Set<UrlaubRefDTO> urlaubRefDTOS = new HashSet<>();
        for (UrlaubRef urlaubRef : urlaubRefs) {
            urlaubRefDTOS.add(
                    new UrlaubRefDTO(
                            urlaubRef.id()
                    )
            );
        }
        return urlaubRefDTOS;
    }

    private Set<KlausurRefDTO> mapKlausurRefDtoToAggregate(Set<KlausurRef> klausurRefs) {
        Set<KlausurRefDTO> klausurRefDTOS = new HashSet<>();
        for (KlausurRef klausurRef : klausurRefs) {
            klausurRefDTOS.add(
                    new KlausurRefDTO(
                            klausurRef.id()
                    )
            );
        }
        return klausurRefDTOS;
    }

}
