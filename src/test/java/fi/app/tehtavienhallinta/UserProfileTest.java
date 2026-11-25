package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** 1:1-relaatio AppUser - UserProfile sekä entiteettien Bean Validation -säännöt. */
@SpringBootTest
@Transactional
@WithMockUser(roles = "ADMIN")
class UserProfileTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    AppUserService service;

    @Autowired
    EntityManager em;

    private static UserProfile profile(String fullName) {
        UserProfile p = new UserProfile();
        p.setFullName(fullName);
        return p;
    }

    private static <T> Set<String> invalidFields(T bean) {
        return VALIDATOR.validate(bean).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void profileIsCreatedAndLoadedWithUser() {
        AppUser u = service.create("profiloitu", "profiloitu@example.com", UUID.randomUUID().toString(),
                Role.USER, profile("Pekka Profiili"));
        em.flush();
        em.clear();

        assertEquals("Pekka Profiili", service.get(u.getId()).getProfile().getFullName());
    }

    @Test
    void updateCreatesMissingProfileAndChangesExisting() {
        AppUser u = service.create("ilmanprofiilia", "ip@example.com", UUID.randomUUID().toString());
        assertNull(u.getProfile());

        service.update(u.getId(), "ilmanprofiilia", "ip@example.com", Role.USER, profile("Eka Nimi"));
        service.update(u.getId(), "ilmanprofiilia", "ip@example.com", Role.USER, profile("Toka Nimi"));
        em.flush();
        em.clear();

        assertEquals("Toka Nimi", service.get(u.getId()).getProfile().getFullName());
    }

    @Test
    void deletingUserDeletesProfile() {
        AppUser u = service.create("poistuva", "poistuva@example.com", UUID.randomUUID().toString(),
                Role.USER, profile("Poistuva Käyttäjä"));
        em.flush();
        Long profileId = u.getProfile().getId();

        service.delete(u.getId());
        em.flush();

        assertNull(em.find(UserProfile.class, profileId));
    }

    @Test
    void profileFieldsAreValidated() {
        UserProfile p = new UserProfile();
        p.setFullName(" ");
        p.setPhone("abc");
        p.setJobTitle("x".repeat(101));
        p.setBio("x".repeat(501));
        p.setWebsite("ei-osoite");

        assertEquals(Set.of("fullName", "phone", "jobTitle", "bio", "website"), invalidFields(p));
    }

    @Test
    void projectFieldsAreValidated() {
        Project p = new Project();
        p.setName("ab");
        p.setDescription("x".repeat(2001));
        p.setBudget(new BigDecimal("-1"));

        assertEquals(Set.of("name", "description", "startDate", "dueDate", "budget"), invalidFields(p));
        p.setName("Kelvollinen");
        p.setDescription(null);
        p.setBudget(new BigDecimal("100.50"));
        p.setStartDate(LocalDate.now());
        p.setDueDate(LocalDate.now());
        assertTrue(invalidFields(p).isEmpty());
    }

    @Test
    void taskFieldsAreValidated() {
        Task t = new Task();
        t.setTitle("ab");
        t.setDescription("x".repeat(2001));
        t.setPriority(6);
        t.setEstimatedHours(-1);

        assertEquals(Set.of("title", "description", "dueDate", "priority", "estimatedHours"), invalidFields(t));
    }

    @Test
    void tagFieldsAreValidated() {
        Tag t = new Tag();
        t.setName("a");
        t.setColor("punainen");
        t.setDescription("x".repeat(201));
        t.setCategory("x".repeat(31));
        t.setSortOrder(1000);

        assertEquals(Set.of("name", "color", "description", "category", "sortOrder"), invalidFields(t));
    }

    @Test
    void userFieldsAreValidated() {
        AppUser u = new AppUser("a b", "ei-sahkoposti", "", null);

        assertEquals(Set.of("username", "email", "passwordHash", "role"), invalidFields(u));
    }
}
