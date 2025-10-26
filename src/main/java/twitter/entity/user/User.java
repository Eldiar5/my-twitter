package twitter.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "usertype")
    protected UserType userType;

    @Column(name = "login")
    protected String login;

    @Column(name = "password_hash")
    protected String passwordHash;

    @Column(name = "registered", insertable = false, updatable = false)
    protected LocalDateTime registrationDate;

    public User() {}

    public abstract String beautify();

    public abstract String userName();

    public abstract String toFile();

}
