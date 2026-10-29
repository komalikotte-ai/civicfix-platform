package com.civicfix.platform.users.entity;

import com.civicfix.platform.users.enums.PermissionType;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "permissions")
public class Permission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private PermissionType name;

    @Column(name = "description")
    private String description;

    public Permission() {
    }

    public Permission(PermissionType name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PermissionType getName() {
        return name;
    }

    public void setName(PermissionType name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
