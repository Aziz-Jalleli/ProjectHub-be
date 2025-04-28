package org.polythec.projecthubbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "priorities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Priority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}