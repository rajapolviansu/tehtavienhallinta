package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Edistynyt tehtävähaku, joka rakentaa dynaamisen {@link CriteriaQuery}:n suoraan Criteria API:lla.
 *
 * <p>Predikaatteja lisätään vain annetuille ehdoille. Kyselyssä on:
 * <ul>
 *   <li>osittainen tekstihaku (LIKE) ja OR-ehto: otsikko TAI kuvaus,</li>
 *   <li>päivämäärävälin rajaus,</li>
 *   <li>JOIN projektiin ja tunnisteisiin (haku relaatioentiteetin ominaisuudella),</li>
 *   <li>rakenne (otsikko OR kuvaus) AND projekti AND tunniste AND ... .</li>
 * </ul>
 */
@Repository
public class TaskSearchRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Task> search(TaskSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Task> query = cb.createQuery(Task.class);
        Root<Task> task = query.from(Task.class);

        List<Predicate> and = new ArrayList<>();

        // (otsikko LIKE x) OR (kuvaus LIKE x)
        if (hasText(c.text())) {
            String pattern = TaskSpecifications.likePattern(c.text());
            and.add(cb.or(
                    cb.like(cb.lower(task.get("title")), pattern, '\\'),
                    cb.like(cb.lower(cb.coalesce(task.<String>get("description"), "")), pattern, '\\')));
        }

        if (c.projectId() != null) {
            and.add(cb.equal(task.get("project").get("id"), c.projectId()));
        }

        // JOIN projektiin: haku projektin nimen perusteella
        if (hasText(c.projectName())) {
            Join<Task, Project> project = task.join("project", JoinType.INNER);
            and.add(cb.like(cb.lower(project.get("name")), TaskSpecifications.likePattern(c.projectName()), '\\'));
        }

        // JOIN tunnisteisiin: yksi join palvelee sekä id- että nimiehtoa
        if (c.tagId() != null || hasText(c.tagName())) {
            Join<Task, Tag> tag = task.join("tags", JoinType.INNER);
            if (c.tagId() != null) {
                and.add(cb.equal(tag.get("id"), c.tagId()));
            }
            if (hasText(c.tagName())) {
                and.add(cb.like(cb.lower(tag.get("name")), TaskSpecifications.likePattern(c.tagName()), '\\'));
            }
        }

        if (c.done() != null) {
            and.add(cb.equal(task.get("done"), c.done()));
        }
        if (c.minPriority() != null) {
            and.add(cb.greaterThanOrEqualTo(task.<Integer>get("priority"), c.minPriority()));
        }
        if (c.dueFrom() != null) {
            and.add(cb.greaterThanOrEqualTo(task.get("dueDate"), c.dueFrom()));
        }
        if (c.dueTo() != null) {
            and.add(cb.lessThanOrEqualTo(task.get("dueDate"), c.dueTo()));
        }

        // Projekti ja tunnisteet haetaan mukaan, jotta käyttöliittymä voi näyttää ne transaktion jälkeen
        // (open-in-view on pois päältä). Fetch on eri join kuin suodatusjoinit, joten se ei rajaa tunnisteita.
        task.fetch("project", JoinType.LEFT);
        task.fetch("tags", JoinType.LEFT);

        // Projekti ja tunnisteet haetaan mukaan, jotta käyttöliittymä voi näyttää ne transaktion jälkeen
        // (open-in-view on pois päältä). Fetch on eri join kuin suodatusjoinit, joten se ei rajaa tunnisteita.
        task.fetch("project", JoinType.LEFT);
        task.fetch("tags", JoinType.LEFT);

        // Sama tehtävä ei saa tulla useaan kertaan, vaikka useampi tunniste täsmäisi
        query.select(task).distinct(true)
                .where(and.toArray(new Predicate[0]))
                .orderBy(cb.asc(task.get("dueDate")), cb.asc(task.get("id")));

        return em.createQuery(query).getResultList();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
