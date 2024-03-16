package checkin.aggregates.urlaub;

import java.util.Objects;

public record UrlaubRef(Long id) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlaubRef urlaubRef = (UrlaubRef) o;
        return Objects.equals(id, urlaubRef.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
