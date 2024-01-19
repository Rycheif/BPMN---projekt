package pl.at.bpmnprojekt.domain.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "App_User", schema = "bpmn_projekt")
public class AppUser {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "app_user_seq")
    @SequenceGenerator(
        name = "app_user_seq",
        schema = "bpmn_projekt",
        initialValue = 6)
    @Column(name = "user_id", nullable = false, unique = true)
    private final Long userId;

    @Column(name = "name", nullable = false, length = 30, unique = true)
    private final String name;

    @Column(name = "email", nullable = false, length = 40, unique = true)
    private final String email;

    @Column(name = "password", nullable = false)
    private final String password;

    public AppUser() {
        this.userId = null;
        this.name = null;
        this.email = null;
        this.password = null;
    }

    public AppUser(Long userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return Objects.equals(userId, appUser.userId)
               && Objects.equals(name, appUser.name)
               && Objects.equals(email, appUser.email)
               && Objects.equals(password, appUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, email, password);
    }

}
