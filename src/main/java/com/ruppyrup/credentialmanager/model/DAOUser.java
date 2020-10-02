package com.ruppyrup.credentialmanager.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Entity
@Table(name = "users")
public class DAOUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    @EqualsAndHashCode.Include
    private String username;

    @Column
    @EqualsAndHashCode.Include
    private String password;

    public DAOUser() {
    }

    public DAOUser(UserDTO userDTO) {
        this.username = userDTO.getUsername();
        this.password = userDTO.getPassword();
    }

    public DAOUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
