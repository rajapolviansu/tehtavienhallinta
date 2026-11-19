package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TagService {

    private final TagRepository tags;

    public TagService(TagRepository tags) {
        this.tags = tags;
    }

    public Tag create(String name, String color) {
        if (tags.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tagi on jo olemassa: " + name);
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        return tags.save(tag);
    }

    @Transactional(readOnly = true)
    public List<Tag> findAll() {
        return tags.findAll();
    }

    @Transactional(readOnly = true)
    public Tag get(Long id) {
        return tags.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tagia ei löytynyt: " + id));
    }

    public Tag update(Long id, String name, String color) {
        Tag tag = get(id);
        if (!name.equalsIgnoreCase(tag.getName()) && tags.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tagi on jo olemassa: " + name);
        }
        tag.setName(name);
        tag.setColor(color);
        return tags.save(tag);
    }

    /** Poistaa tagin ja irrottaa sen kaikista tehtävistä. */
    public void delete(Long id) {
        Tag tag = get(id);
        for (Task task : new ArrayList<>(tag.getTasks())) {
            task.removeTag(tag);
        }
        tags.delete(tag);
    }
}
