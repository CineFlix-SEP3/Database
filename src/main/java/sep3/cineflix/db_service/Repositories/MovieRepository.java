package sep3.cineflix.db_service.Repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sep3.cineflix.db_service.Entities.Movie;

import java.util.Optional;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    Optional<Movie> findByTitle(String title);

    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    List<Movie> findByGenresContaining(String genre);

    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    List<Movie> findByDirectorsContaining(String director);

    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    List<Movie> findByActorsContaining(String actor);

    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    List<Movie> findAll();

    @EntityGraph(attributePaths = {"genres", "directors", "actors"})
    Optional<Movie> findById(Integer id);

    boolean existsByTitle(String title);
}