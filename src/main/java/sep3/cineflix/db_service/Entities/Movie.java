package sep3.cineflix.db_service.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255, unique = true)
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private List<String> genres;

    @ElementCollection
    @CollectionTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "director")
    private List<String> directors;

    @ElementCollection
    @CollectionTable(name = "movie_actors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "actor")
    private List<String> actors;

    @Column(name = "run_time", nullable = false)
    @NotNull(message = "Run time must be specified")
    private Integer runTime;

    @Column(name = "release_date", nullable = false)
    @NotNull(message = "Release date must be specified")
    private LocalDate releaseDate;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    private Double rating;

    @Lob
    @Column(length = 1000)
    private String description;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;
}