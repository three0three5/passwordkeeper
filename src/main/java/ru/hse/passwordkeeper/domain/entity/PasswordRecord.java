package ru.hse.passwordkeeper.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "password_record", indexes = {
        @Index(name = "owner_id_index", columnList = "id, owner")
})
@Entity
public class PasswordRecord {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "name")
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner", referencedColumnName="login")
    private UserEntity owner;

    @Column(name = "login")
    private String login;

    @Column(name = "password_value")
    private String password;

    @Column(name = "url")
    private String url;

    @ManyToOne
    private DirectoryEntity directory;
}
