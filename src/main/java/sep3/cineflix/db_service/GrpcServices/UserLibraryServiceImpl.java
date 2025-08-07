package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sep3.cineflix.db_service.Entities.UserFavorites;
import sep3.cineflix.db_service.Entities.UserWatchList;
import sep3.cineflix.db_service.Repositories.UserFavoritesRepository;
import sep3.cineflix.db_service.Repositories.UserWatchListRepository;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.db_service.Repositories.MovieRepository;
import sep3.cineflix.grpc.userlibrary.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserLibraryServiceImpl extends UserLibraryServiceGrpc.UserLibraryServiceImplBase {

    private final UserFavoritesRepository favoritesRepository;
    private final UserWatchListRepository watchListRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public UserLibraryServiceImpl(
            UserFavoritesRepository favoritesRepository,
            UserWatchListRepository watchListRepository,
            UserRepository userRepository,
            MovieRepository movieRepository
    ) {
        this.favoritesRepository = favoritesRepository;
        this.watchListRepository = watchListRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    public void addFavorite(AddFavoriteRequest request, StreamObserver<AddFavoriteResponse> responseObserver) {
        var userOpt = userRepository.findById(request.getUserId());
        var movieOpt = movieRepository.findById(request.getMovieId());
        if (userOpt.isEmpty() || movieOpt.isEmpty()) {
            responseObserver.onNext(AddFavoriteResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }
        if (favoritesRepository.existsByUserIdAndMovieId(request.getUserId(), request.getMovieId())) {
            responseObserver.onNext(AddFavoriteResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }
        UserFavorites favorite = new UserFavorites(null, userOpt.get(), movieOpt.get(), LocalDateTime.now());
        favoritesRepository.save(favorite);
        responseObserver.onNext(AddFavoriteResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFavorites(GetFavoritesRequest request, StreamObserver<GetFavoritesResponse> responseObserver) {
        List<UserFavorites> favorites = favoritesRepository.findByUserId(request.getUserId());
        GetFavoritesResponse.Builder builder = GetFavoritesResponse.newBuilder();
        for (UserFavorites fav : favorites) {
            builder.addMovieIds(fav.getMovie().getId());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void removeFavorite(RemoveFavoriteRequest request, StreamObserver<RemoveFavoriteResponse> responseObserver) {
        favoritesRepository.deleteByUserIdAndMovieId(request.getUserId(), request.getMovieId());
        responseObserver.onNext(RemoveFavoriteResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void addWatchList(AddWatchListRequest request, StreamObserver<AddWatchListResponse> responseObserver) {
        var userOpt = userRepository.findById(request.getUserId());
        var movieOpt = movieRepository.findById(request.getMovieId());
        if (userOpt.isEmpty() || movieOpt.isEmpty()) {
            responseObserver.onNext(AddWatchListResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }
        if (watchListRepository.existsByUserIdAndMovieId(request.getUserId(), request.getMovieId())) {
            responseObserver.onNext(AddWatchListResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
            return;
        }
        UserWatchList watchList = new UserWatchList(null, userOpt.get(), movieOpt.get(), LocalDateTime.now());
        watchListRepository.save(watchList);
        responseObserver.onNext(AddWatchListResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getWatchList(GetWatchListRequest request, StreamObserver<GetWatchListResponse> responseObserver) {
        List<UserWatchList> watchList = watchListRepository.findByUserId(request.getUserId());
        GetWatchListResponse.Builder builder = GetWatchListResponse.newBuilder();
        for (UserWatchList wl : watchList) {
            builder.addMovieIds(wl.getMovie().getId());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void removeWatchList(RemoveWatchListRequest request, StreamObserver<RemoveWatchListResponse> responseObserver) {
        watchListRepository.deleteByUserIdAndMovieId(request.getUserId(), request.getMovieId());
        responseObserver.onNext(RemoveWatchListResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }
}