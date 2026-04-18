package com.plataforma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Long en lugar de long — mejor práctica con JPA y null checks

    @Column(unique = true)
    private String email;

    private String password;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    private boolean active = true;

    public boolean hasPermission(String permissionName)
    {
        if (this.role == null || this.role.getPermissions() == null)
            return false;
        return this.role.getPermissions().stream()
            .anyMatch(p -> p.getName().equals(permissionName));
    }
}