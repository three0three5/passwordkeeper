package ru.hse.passwordkeeper.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DirectoryEntity {
    @Id
    private String id;

    private String name;

    @ManyToOne
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    private DirectoryEntity parent;
}
