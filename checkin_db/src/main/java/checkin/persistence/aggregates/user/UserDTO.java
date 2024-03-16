package checkin.persistence.aggregates.user;


import checkin.persistence.aggregates.klausur.KlausurRefDTO;
import checkin.persistence.aggregates.urlaub.UrlaubRefDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import java.util.Objects;
import java.util.Set;

public class UserDTO implements Persistable<String> {

    @Id
    private final String githubID;

    private final Set<UrlaubRefDTO> urlaube;

    private final Set<KlausurRefDTO> klausuren;

    private Integer feld;

    @Transient
    private final boolean isNew;

    public String getGithubID() {
        return githubID;
    }

    public Set<UrlaubRefDTO> getUrlaube() {
        return urlaube;
    }

    public Set<KlausurRefDTO> getKlausuren() {
        return klausuren;
    }

    public UserDTO(String githubID, Set<UrlaubRefDTO> urlaube, Set<KlausurRefDTO> klausuren, boolean isNew) {
        this.githubID = githubID;
        this.urlaube = urlaube;
        this.klausuren = klausuren;
        this.isNew = isNew;
    }

    @PersistenceConstructor
    public UserDTO(String githubID, Set<UrlaubRefDTO> urlaube, Set<KlausurRefDTO> klausuren) {
        this.githubID = githubID;
        this.urlaube = urlaube;
        this.klausuren = klausuren;
        this.isNew = false;
    }

    public void setFeld(int feld) {
        this.feld = feld;
    }

    @Override
    public String getId() {
        return this.githubID;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "githubID='" + githubID + '\'' +
                ", urlaube=" + urlaube +
                ", klausuren=" + klausuren +
                ", isNew=" + isNew +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return isNew == userDTO.isNew && Objects.equals(githubID, userDTO.githubID) && Objects.equals(urlaube, userDTO.urlaube) && Objects.equals(klausuren, userDTO.klausuren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(githubID, urlaube, klausuren, isNew);
    }
}
