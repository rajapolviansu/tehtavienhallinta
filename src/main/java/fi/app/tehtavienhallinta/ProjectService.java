package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projects;

    public ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    public Project create(Project project) {
        return projects.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projects.findAll();
    }

    @Transactional(readOnly = true)
    public List<Project> findActive() {
        return projects.findByArchivedFalseOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Project get(Long id) {
        return projects.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projektia ei löytynyt: " + id));
    }

    /** Päivittää projektin kentät (ei tehtäviä). */
    public Project update(Long id, Project changes) {
        Project project = get(id);
        project.setName(changes.getName());
        project.setDescription(changes.getDescription());
        project.setStartDate(changes.getStartDate());
        project.setDueDate(changes.getDueDate());
        project.setArchived(changes.isArchived());
        return projects.save(project);
    }

    /** Poistaa projektin ja sen tehtävät. */
    public void delete(Long id) {
        projects.delete(get(id));
    }
}
