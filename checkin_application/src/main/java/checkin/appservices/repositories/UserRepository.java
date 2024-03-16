package checkin.appservices.repositories;

import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;

import java.util.List;

public interface UserRepository {

    User findByGithubId(String githubId);

    List<Klausur> findKlausuren(String githubID);

    List<Urlaub> findUrlaube(String githubID);

    void save(User user);

    void update(User user);
}
