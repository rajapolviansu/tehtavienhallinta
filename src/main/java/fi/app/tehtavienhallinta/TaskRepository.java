package fi.app.tehtavienhallinta;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /** Hakee projektin ja tunnisteet mukaan, jotta käyttöliittymä voi näyttää ne transaktion jälkeen. */
    @Override
    @EntityGraph(attributePaths = {"project", "tags"})
    List<Task> findAll(Specification<Task> spec, Sort sort);

    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<Task> findByProjectIdAndDoneFalse(Long projectId);

    List<Task> findByTagsId(Long tagId);

    long countByProjectIdAndDoneFalse(Long projectId);
}
