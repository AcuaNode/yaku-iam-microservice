package io.github.rafaviv.yakubackend.iam.domain.model.aggregates;

import io.github.rafaviv.yakubackend.iam.domain.model.entities.Role;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.Email;
import io.github.rafaviv.yakubackend.iam.domain.model.valueobjects.HashedPassword;
import io.github.rafaviv.yakubackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 
 * User aggregate root for IAM context
 * This class represents the aggregate root for the User entity with multitenancy support.
 *
 * @see AuditableAbstractAggregateRoot
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(unique = true, nullable = false)
    private String username;

    @Embedded
    private Email email;

    @Embedded
    private HashedPassword password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @Column
    private Long assignedFarmId;

    protected User() {
        this.roles = new ArrayList<>();
    }

    /**
     * Constructor para el agregado User
     * Solo contiene información de autenticación y autorización (IAM)
     * @param username el nombre de usuario
     * @param email el correo electrónico
     * @param password el hash de la contraseña
     * @param firstName el nombre
     * @param lastName el apellido
     * @param isVerified si está verificado
     */
    public User(String username, Email email, HashedPassword password, String firstName, String lastName, boolean isVerified) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isVerified = isVerified;
        this.roles = new ArrayList<>();
    }
    
    public void addRole(Role role) {
        if (role != null && !this.roles.contains(role)) {
            this.roles.add(role);
        }
    }
    
    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }

    public String getEmailAddress() {
        return email.address();
    }

    public String getPasswordHash() {
        return password.hash();
    }
}
