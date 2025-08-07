package sep3.cineflix.db_service.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_watchlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWatchList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "added_on", nullable = false)
    private LocalDateTime addedOn;
}