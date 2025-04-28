package org.polythec.projecthubbe.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "To Do", "In Progress", "Done"
}

