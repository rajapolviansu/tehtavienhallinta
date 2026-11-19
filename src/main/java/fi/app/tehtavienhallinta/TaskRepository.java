package fi.app.tehtavienhallinta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<Task> findByProjectIdAndDoneFalse(Long projectId);

    List<Task> findByTagsId(Long tagId);

    long countByProjectIdAndDoneFalse(Long projectId);
}
