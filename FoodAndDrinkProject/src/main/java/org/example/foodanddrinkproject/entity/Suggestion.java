package org.example.foodanddrinkproject.entity;

import org.example.foodanddrinkproject.enums.SuggestionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "suggestions")
@Getter
@Setter
public class Suggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "suggestion_text", nullable = false)
    private String suggestionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
