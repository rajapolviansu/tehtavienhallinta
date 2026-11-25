package fi.app.tehtavienhallinta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Käyttäjän profiilitiedot. Jokaisella käyttäjällä on enintään yksi profiili (1:1-relaatio AppUseriin).
 */
@Entity
@Table(name = "user_profile")
public class UserProfile {

    public static final String PHONE_PATTERN = "^[+]?[0-9 ()-]{6,20}$";
    public static final String WEBSITE_PATTERN = "^https?://[^ ]+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String fullName;

    @Pattern(regexp = PHONE_PATTERN, message = "Anna kelvollinen puhelinnumero")
    @Column(length = 20)
    private String phone;

    @Size(max = 100)
    @Column(length = 100)
    private String jobTitle;

    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @Pattern(regexp = WEBSITE_PATTERN, message = "Osoitteen tulee alkaa http:// tai https://")
    @Size(max = 200)
    @Column(length = 200)
    private String website;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
