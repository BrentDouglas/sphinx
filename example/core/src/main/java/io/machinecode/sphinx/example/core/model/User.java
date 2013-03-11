package io.machinecode.sphinx.example.core.model;

import io.machinecode.sphinx.example.core.response.FacebookResponse;
import io.machinecode.sphinx.example.core.response.TwitterResponse;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.security.Principal;

@Entity
@Table(schema = "public", name = "user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"facebook_id", "twitter_id", "email"})
)
@NamedQueries(value = {
        @NamedQuery(name = "User.withUsername", query = "select u from User u where u.username=:username"),
        @NamedQuery(name = "User.withFacebookId", query = "select u from User u where u.facebookId=:facebookId"),
        @NamedQuery(name = "User.withTwitterId", query = "select u from User u where u.twitterId=:twitterId")
})
public class User implements Principal {

    private Integer id;
    private Integer facebookId;
    private Integer twitterId;
    private String name;
    private String username;
    private String password;
    private String email;
    private String salt;

    public User() {

    }

    public User(final TwitterResponse response) {
        this.twitterId = response.getId();
        this.name = response.getName();
    }

    public User(final FacebookResponse response) {
        this.facebookId = response.getId();
        this.name = response.getName();
        this.email = response.getEmail();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Column(name = "facebook_id", nullable = true, unique = true)
    public Integer getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(final Integer facebookId) {
        this.facebookId = facebookId;
    }

    @Column(name = "twitter_id", nullable = true, unique = true)
    public Integer getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(final Integer twitterId) {
        this.twitterId = twitterId;
    }

    @NotNull
    @NotEmpty
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @NotNull
    @NotEmpty
    @Size(min = 1)
    @Column(name = "username", nullable = false, unique = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @NotNull
    @NotEmpty
    @Size(min = 8)
    @Column(name = "password", nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @NotNull
    @NotEmpty
    @Email
    @Column(name = "email", nullable = false, unique = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @NotNull
    @NotEmpty
    @Column(name = "salt", nullable = false)
    public String getSalt() {
        return salt;
    }

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "User [id:" + id + ",email:" + email + ",name:" + name + "]";
    }
}