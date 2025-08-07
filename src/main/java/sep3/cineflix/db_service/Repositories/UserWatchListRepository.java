package sep3.cineflix.db_service.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sep3.cineflix.db_service.Entities.UserWatchList;

import java.util.List;

@Repository
public interface UserWatchListRepository extends JpaRepository<UserWatchList, Integer> {
    List<UserWatchList> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
    void deleteByUserIdAndMovieId(Integer userId, Integer movieId);
    boolean existsByUserIdAndMovieId(Integer userId, Integer movieId);
}