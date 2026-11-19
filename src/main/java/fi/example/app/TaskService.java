package fi.example.app;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectService projects;
    private final TagService tags;

    public TaskService(TaskRepository tasks, ProjectService projects, TagService tags) {
        this.tasks = tasks;
        this.projects = projects;
        this.tags = tags;
    }

    /** Luo tehtävän annettuun projektiin. */
    public Task create(Long projectId, Task task) {
        projects.get(projectId).addTask(task);
        return tasks.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return tasks.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> findByProject(Long projectId) {
        return tasks.findByProjectIdOrderByDueDateAsc(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByTag(Long tagId) {
        return tasks.findByTagsId(tagId);
    }

    @Transactional(readOnly = true)
    public Task get(Long id) {
        return tasks.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tehtävää ei löytynyt: " + id));
    }

    /** Päivittää tehtävän kentät (ei projektia eikä tageja). */
    public Task update(Long id, Task changes) {
        Task task = get(id);
        task.setTitle(changes.getTitle());
        task.setDescription(changes.getDescription());
        task.setDueDate(changes.getDueDate());
        task.setDone(changes.isDone());
        return tasks.save(task);
    }

    public Task setDone(Long id, boolean done) {
        Task task = get(id);
        task.setDone(done);
        return tasks.save(task);
    }

    /** Siirtää tehtävän toiseen projektiin. */
    public Task moveToProject(Long taskId, Long projectId) {
        Task task = get(taskId);
        Project target = projects.get(projectId);
        if (task.getProject() != null) {
            task.getProject().getTasks().remove(task);
        }
        target.addTask(task);
        return tasks.save(task);
    }

    public Task addTag(Long taskId, Long tagId) {
        Task task = get(taskId);
        task.addTag(tags.get(tagId));
        return task;
    }

    public Task removeTag(Long taskId, Long tagId) {
        Task task = get(taskId);
        task.removeTag(tags.get(tagId));
        return task;
    }

    public void delete(Long id) {
        Task task = get(id);
        if (task.getProject() != null) {
            task.getProject().removeTask(task);
        }
        tasks.delete(task);
    }
}
