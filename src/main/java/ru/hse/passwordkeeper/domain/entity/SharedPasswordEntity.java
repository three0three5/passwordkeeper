package ru.hse.passwordkeeper.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "shared_password")
@Getter
@Setter
@NoArgsConstructor
public class SharedPasswordEntity {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "creator", referencedColumnName = "login", nullable = false)
    private UserEntity owner;

    @ManyToOne
    @JoinColumn(name = "to_share", referencedColumnName = "id", nullable = false)
    private PasswordRecord toShare;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "shared_with", referencedColumnName = "login")
    private UserEntity sharedWith;
}
