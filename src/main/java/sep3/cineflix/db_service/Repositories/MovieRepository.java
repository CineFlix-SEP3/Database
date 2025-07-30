package sep3.cineflix.db_service.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sep3.cineflix.db_service.Entities.Movie;

import java.util.Optional;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Optional<Movie> findByTitle(String title);
    List<Movie> findByGenresContaining(String genre);
    List<Movie> findByDirectorsContaining(String director);
    List<Movie> findByActorsContaining(String actor);
    boolean existsByTitle(String title);
}