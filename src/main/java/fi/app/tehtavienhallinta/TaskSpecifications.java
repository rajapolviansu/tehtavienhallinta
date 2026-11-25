package fi.app.tehtavienhallinta;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Tehtävien suodatusehdot. Jokainen ehto palauttaa "kaikki", jos parametri on null,
 * joten ehdot voi yhdistää vapaasti ({@code and}).
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    /** Muodostaa pienillä kirjaimilla LIKE-kuvion ("%teksti%"); jokerimerkit % ja _ käsitellään kirjaimellisina (escape '\'). */
    static String likePattern(String text) {
        return "%" + text.trim().toLowerCase().replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }

    public static Specification<Task> inProject(Long projectId) {
        return (root, query, cb) -> projectId == null
                ? cb.conjunction()
                : cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasTag(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.join("tags", JoinType.INNER).get("id"), tagId);
        };
    }

    public static Specification<Task> isDone(Boolean done) {
        return (root, query, cb) -> done == null
                ? cb.conjunction()
                : cb.equal(root.get("done"), done);
    }

    public static Specification<Task> dueFrom(LocalDate from) {
        return (root, query, cb) -> from == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("dueDate"), from);
    }

    public static Specification<Task> dueTo(LocalDate to) {
        return (root, query, cb) -> to == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("dueDate"), to);
    }

    /** Hakee otsikosta tai kuvauksesta, kirjainkoosta riippumatta. */
    public static Specification<Task> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = likePattern(text);
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern, '\\'),
                    cb.like(cb.lower(cb.coalesce(root.<String>get("description"), "")), pattern, '\\'));
        };
    }

    /** Vain myöhässä olevat: erääntynyt ennen annettua päivää eikä valmis. */
    public static Specification<Task> overdue(LocalDate today) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("done")),
                cb.lessThan(root.get("dueDate"), today));
    }
}
