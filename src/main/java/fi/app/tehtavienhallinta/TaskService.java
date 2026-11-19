package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    /** Hakee tehtävät annetuilla kriteereillä, eräpäivän mukaan järjestettynä. */
    @Transactional(readOnly = true)
    public List<Task> search(TaskFilter filter) {
        return tasks.findAll(filter.toSpecification(),
                Sort.by(Sort.Order.asc("dueDate"), Sort.Order.asc("id")));
    }

    /** Palauttaa myöhässä olevat (erääntyneet ja keskeneräiset) tehtävät. */
    @Transactional(readOnly = true)
    public List<Task> findOverdue(LocalDate today) {
        return tasks.findAll(TaskSpecifications.overdue(today), Sort.by("dueDate"));
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

    /** Luo tehtävän projektiin ja asettaa sille tunnisteet yhdessä transaktiossa. */
    public Task create(Long projectId, Task task, Set<Long> tagIds) {
        Task saved = create(projectId, task);
        syncTags(saved, tagIds);
        return saved;
    }

    /** Päivittää tehtävän kentät, projektin ja tunnisteet yhdessä transaktiossa. */
    public Task update(Long id, Task changes, Long projectId, Set<Long> tagIds) {
        Task task = update(id, changes);
        if (task.getProject() == null || !task.getProject().getId().equals(projectId)) {
            moveToProject(id, projectId);
        }
        syncTags(task, tagIds);
        return task;
    }

    private void syncTags(Task task, Set<Long> tagIds) {
        task.getTags().stream()
                .filter(t -> !tagIds.contains(t.getId()))
                .toList()
                .forEach(task::removeTag);
        for (Long tagId : tagIds) {
            task.addTag(tags.get(tagId));
        }
    }

    public void delete(Long id) {
        Task task = get(id);
        if (task.getProject() != null) {
            task.getProject().removeTask(task);
        }
        tasks.delete(task);
    }
}
