package ir.tiroon.microservices.model.userManagement;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "User")
@Proxy(lazy = false)
public class User implements Serializable {

    @Id
    @Column(nullable = false, unique = true)
    String phoneNumber;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String password;

    @Column(unique = true, nullable = false)
    String email;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "authorities",
            joinColumns = {@JoinColumn(name = "phoneNumber")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    State state = State.Active;

    public User() {
    }

    @JsonCreator
    public User(@JsonProperty("name") String name, @JsonProperty("password") String password,
                @JsonProperty("email") String email, @JsonProperty("phoneNumber") String phone) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.phoneNumber = phone;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
