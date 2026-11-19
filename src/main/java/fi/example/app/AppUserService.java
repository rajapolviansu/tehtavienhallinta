package fi.example.app;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository users;

    public AppUserService(AppUserRepository users) {
        this.users = users;
    }

    public AppUser create(String username, String email) {
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        return users.save(new AppUser(username, email));
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

    public AppUser update(Long id, String username, String email) {
        AppUser user = get(id);
        if (!username.equals(user.getUsername()) && users.existsByUsername(username)) {
            throw new IllegalArgumentException("Käyttäjätunnus on jo käytössä: " + username);
        }
        if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
            throw new IllegalArgumentException("Sähköposti on jo käytössä: " + email);
        }
        user.setUsername(username);
        user.setEmail(email);
        return users.save(user);
    }

    public void delete(Long id) {
        users.delete(get(id));
    }
}
