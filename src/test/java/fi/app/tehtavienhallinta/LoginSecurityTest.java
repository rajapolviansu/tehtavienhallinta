package fi.app.tehtavienhallinta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kirjautuminen koko suodatinketjun läpi. Ei @Transactional, koska MockMvc-kutsu
 * ja käyttäjän luonti kulkevat eri säikeissä/transaktioissa; käyttäjä siivotaan itse.
 */
@SpringBootTest
class LoginSecurityTest {

    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    WebApplicationContext context;

    // Käyttäjä luodaan suoraan repositoriolla: AppUserService vaatii ADMIN-roolin, ja
    // mock-käyttäjä sotkisi kirjautumistestit (pyynnöt eivät olisi enää kirjautumattomia).
    @Autowired
    AppUserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    MockMvc mvc;
    Long userId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        userId = users.save(new AppUser("login-testaaja", "login-testaaja@example.com",
                passwordEncoder.encode(PASSWORD))).getId();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        users.deleteById(userId);
    }

    @Test
    void validCredentialsAuthenticate() throws Exception {
        mvc.perform(formLogin().user("login-testaaja").password(PASSWORD))
                .andExpect(authenticated().withUsername("login-testaaja"));
    }

    @Test
    void wrongPasswordDoesNotAuthenticate() throws Exception {
        mvc.perform(formLogin().user("login-testaaja").password("vaara-salasana"))
                .andExpect(unauthenticated());
    }

    @Test
    void unknownUserDoesNotAuthenticate() throws Exception {
        mvc.perform(formLogin().user("ei-ole-olemassa").password(PASSWORD))
                .andExpect(unauthenticated());
    }

    @Test
    void logoutEndsSession() throws Exception {
        var session = mvc.perform(formLogin().user("login-testaaja").password(PASSWORD))
                .andExpect(authenticated())
                .andReturn().getRequest().getSession();

        mvc.perform(post("/logout").with(csrf()).session((MockHttpSession) session))
                .andExpect(unauthenticated());
    }

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }
}
