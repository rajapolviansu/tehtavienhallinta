package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Käyttäjien hallinta. Kaikki toiminnot vaativat ADMIN-roolin.
 */
@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AppUserService {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Luo tavallisen käyttäjän (rooli USER). */
    public AppUser create(String username, String email, String rawPassword) {
        return create(username, email, rawPassword, Role.USER);
    }

    public AppUser create(String username, String email, String rawPassword, Role role) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Salasanan tulee olla vähintään " + MIN_PASSWORD_LENGTH + " merkkiä pitkä");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        return users.save(new AppUser(username, email, passwordEncoder.encode(rawPassword), role));
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser get(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Käyttäjää ei löytynyt: " + id));
    }

    /** Päivittää tunnuksen ja sähköpostin; rooli säilyy ennallaan. */
    public AppUser update(Long id, String username, String email) {
        return update(id, username, email, get(id).getRole());
    }

    public AppUser update(Long id, String username, String email, Role role) {
        AppUser user = get(id);
        if (!username.equals(user.getUsername()) && users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            requireAnotherAdmin();
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        return users.save(user);
    }

    public void delete(Long id) {
        AppUser user = get(id);
        if (user.getRole() == Role.ADMIN) {
            requireAnotherAdmin();
        }
        users.delete(user);
    }

    /** Estää viimeisen ylläpitäjän poistamisen tai alentamisen, jottei kukaan lukitse itseään ulos. */
    private void requireAnotherAdmin() {
        if (users.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Viimeistä ylläpitäjää ei voi poistaa tai alentaa");
        }
    }
}
