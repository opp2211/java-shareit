package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String description;

    @OneToOne
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    private LocalDateTime created = LocalDateTime.now();
}
