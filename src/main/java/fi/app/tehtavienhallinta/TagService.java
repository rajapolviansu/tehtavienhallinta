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

    /** Apumetodi: luo tagin pelkällä nimellä ja värillä. */
    public Tag create(String name, String color) {
        Tag data = new Tag();
        data.setName(name);
        data.setColor(color);
        return create(data);
    }

    public Tag update(Long id, String name, String color) {
        Tag data = new Tag();
        data.setName(name);
        data.setColor(color);
        data.setSortOrder(get(id).getSortOrder());
        return update(id, data);
    }

    public Tag create(Tag data) {
        if (tags.existsByNameIgnoreCase(data.getName())) {
            throw new IllegalArgumentException("Tagi on jo olemassa: " + data.getName());
        }
        Tag tag = new Tag();
        copyFields(data, tag);
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

    public Tag update(Long id, Tag data) {
        Tag tag = get(id);
        if (!data.getName().equalsIgnoreCase(tag.getName()) && tags.existsByNameIgnoreCase(data.getName())) {
            throw new IllegalArgumentException("Tagi on jo olemassa: " + data.getName());
        }
        copyFields(data, tag);
        return tags.save(tag);
    }

    private static void copyFields(Tag from, Tag to) {
        to.setName(from.getName());
        to.setColor(from.getColor());
        to.setDescription(from.getDescription());
        to.setCategory(from.getCategory());
        to.setSortOrder(from.getSortOrder());
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
