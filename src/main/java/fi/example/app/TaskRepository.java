package fi.example.app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);

    List<Task> findByProjectIdAndDoneFalse(Long projectId);

    long countByProjectIdAndDoneFalse(Long projectId);
}
