package org.polythec.projecthubbe.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idprojet;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // OWNER: Many projects can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // MEMBERS: Many users can be part of many projects
    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    // Add helper method to add members
    public void addMember(User user) {
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public void addMemberPerProject(User user, String role) {
        ProjectMember pm = new ProjectMember();
        pm.setProject(this);
        pm.setUser(user);
        pm.setRole(role);
        pm.setId(new ProjectMemberId(this.idprojet, user.getId()));
        projectMembers.add(pm);
    }

}
