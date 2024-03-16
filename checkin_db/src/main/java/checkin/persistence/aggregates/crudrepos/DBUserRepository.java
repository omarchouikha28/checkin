package checkin.persistence.aggregates.crudrepos;

import checkin.persistence.aggregates.klausur.KlausurDTO;
import checkin.persistence.aggregates.urlaub.UrlaubDTO;
import checkin.persistence.aggregates.user.UserDTO;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DBUserRepository extends CrudRepository<UserDTO, String> {

    UserDTO findUserDTOByGithubID(String githubID);

    @Query("""
         SELECT kl.*
         FROM klausur_dto kl, user_dto u, klausur_ref_dto kref
         WHERE kl.id = kref.klausur_dto
         AND kref.user_dto = u.github_id
         AND kref.user_dto LIKE :githubID
         ORDER BY von
         """)
    List<KlausurDTO> findKlausuren(@Param("githubID") String githubID);

    @Query("""
         SELECT ul.*
         FROM urlaub_dto ul, user_dto u, urlaub_ref_dto uref
         WHERE ul.id = uref.urlaub_dto
         AND uref.user_dto = u.github_id
         AND uref.user_dto LIKE :githubID
         ORDER BY datum, von
         """)
    List<UrlaubDTO> findUrlaube(@Param("githubID") String githubID);

}
