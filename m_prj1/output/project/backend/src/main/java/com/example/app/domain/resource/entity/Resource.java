package com.example.app.domain.resource.entity;

import com.example.app.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Resource create(String name, String description, User owner) {
        Resource resource = new Resource();
        resource.name = name;
        resource.description = description;
        resource.owner = owner;
        return resource;
    }

    public void update(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        if (status != null) this.status = status;
    }

    public void delete() {
        this.status = Status.DELETED;
    }

    public enum Status { ACTIVE, INACTIVE, DELETED }
}
