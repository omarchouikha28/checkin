package checkin.aggregates.user;

import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.klausur.KlausurRef;
import checkin.aggregates.urlaub.UrlaubRef;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User {

    private final String githubID;
    private final Set<UrlaubRef> urlaube = new HashSet<>();
    private final Set<KlausurRef> klausuren = new HashSet<>();
    private int resturlaub = 240;
    private int summeUrlaube = 0;

    public int getResturlaub() {
        return resturlaub;
    }

    public int getSummeUrlaube() {
        return summeUrlaube;
    }

    public User(String githubID) {
        this.githubID = githubID;
    }

    public void addUrlaub(Urlaub urlaub) {
        urlaube.add(new UrlaubRef(urlaub.id()));
        resturlaub = resturlaub - urlaub.zeitspanne().berechneDauer();
        summeUrlaube = summeUrlaube + urlaub.zeitspanne().berechneDauer();
    }

    public void addKlausur(Klausur klausur) { klausuren.add(new KlausurRef(klausur.id())); }

    public void deleteUrlaub(Urlaub urlaub){
        urlaube.remove(new UrlaubRef(urlaub.id()));
        resturlaub = resturlaub + urlaub.zeitspanne().berechneDauer();
        summeUrlaube = summeUrlaube - urlaub.zeitspanne().berechneDauer();
    }

    public void deleteKlausur(Klausur klausur){
        klausuren.remove(new KlausurRef(klausur.id()));
    }

    public String getGithubID() {
        return githubID;
    }

    public Set<UrlaubRef> getUrlaube() {
        return urlaube;
    }

    public Set<KlausurRef> getKlausuren() {
        return klausuren;
    }

    @Override
    public String toString() {
        return "User{" +
                "githubID='" + githubID + '\'' +
                ", urlaube=" + urlaube +
                ", klausuren=" + klausuren +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return githubID.equals(user.githubID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(githubID, urlaube, klausuren);
    }
}
