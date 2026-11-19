package fi.example.app;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Tehtävien hakukriteerit. Kaikki kentät ovat valinnaisia (null = ei rajausta).
 */
public record TaskFilter(
        Long projectId,
        Long tagId,
        Boolean done,
        LocalDate dueFrom,
        LocalDate dueTo,
        String text) {

    public static TaskFilter none() {
        return new TaskFilter(null, null, null, null, null, null);
    }

    public Specification<Task> toSpecification() {
        return Specification
                .where(TaskSpecifications.inProject(projectId))
                .and(TaskSpecifications.hasTag(tagId))
                .and(TaskSpecifications.isDone(done))
                .and(TaskSpecifications.dueFrom(dueFrom))
                .and(TaskSpecifications.dueTo(dueTo))
                .and(TaskSpecifications.textContains(text));
    }
}
