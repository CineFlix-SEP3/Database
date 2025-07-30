package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import sep3.cineflix.db_service.Entities.Movie;
import sep3.cineflix.db_service.Repositories.MovieRepository;
import sep3.cineflix.grpc.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class MovieServiceImpl extends MovieServiceGrpc.MovieServiceImplBase {

    private final MovieRepository movieRepository;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public void createMovie(CreateMovieRequest request, StreamObserver<MovieResponse> responseObserver) {
        if (movieRepository.existsByTitle(request.getTitle())) {
            responseObserver.onError(new RuntimeException("Title already exists"));
            return;
        }
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .genres(request.getGenresList())
                .directors(request.getDirectorsList())
                .actors(request.getActorsList())
                .runTime(request.getRunTime())
                .releaseDate(LocalDate.parse(request.getReleaseDate()))
                .rating(request.getRating())
                .description(request.getDescription())
                .posterUrl(request.getPosterUrl())
                .build();
        Movie saved = movieRepository.save(movie);
        responseObserver.onNext(toMovieResponse(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void getMovieById(GetMovieByIdRequest request, StreamObserver<MovieResponse> responseObserver) {
        Optional<Movie> movieOpt = movieRepository.findById(request.getId());
        movieOpt.ifPresentOrElse(
                movie -> {
                    responseObserver.onNext(toMovieResponse(movie));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(new RuntimeException("Movie not found"))
        );
    }

    @Override
    public void getMovieByTitle(GetMovieByTitleRequest request, StreamObserver<MovieResponse> responseObserver) {
        Optional<Movie> movieOpt = movieRepository.findByTitle(request.getTitle());
        movieOpt.ifPresentOrElse(
                movie -> {
                    responseObserver.onNext(toMovieResponse(movie));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(new RuntimeException("Movie not found"))
        );
    }

    @Override
    public void getAllMovies(GetAllMoviesRequest request, StreamObserver<GetAllMoviesResponse> responseObserver) {
        var movies = movieRepository.findAll().stream()
                .map(this::toMovieResponse)
                .collect(Collectors.toList());
        GetAllMoviesResponse response = GetAllMoviesResponse.newBuilder()
                .addAllMovies(movies)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateMovie(UpdateMovieRequest request, StreamObserver<MovieResponse> responseObserver) {
        Optional<Movie> movieOpt = movieRepository.findById(request.getId());
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            if (!movie.getTitle().equals(request.getTitle()) && movieRepository.existsByTitle(request.getTitle())) {
                responseObserver.onError(new RuntimeException("Title already exists"));
                return;
            }
            movie.setTitle(request.getTitle());
            movie.setGenres(request.getGenresList());
            movie.setDirectors(request.getDirectorsList());
            movie.setActors(request.getActorsList());
            movie.setRunTime(request.getRunTime());
            movie.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
            movie.setRating(request.getRating());
            movie.setDescription(request.getDescription());
            movie.setPosterUrl(request.getPosterUrl());
            Movie updated = movieRepository.save(movie);
            responseObserver.onNext(toMovieResponse(updated));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Movie not found"));
        }
    }

    @Override
    public void deleteMovie(DeleteMovieRequest request, StreamObserver<DeleteMovieResponse> responseObserver) {
        if (movieRepository.existsById(request.getId())) {
            movieRepository.deleteById(request.getId());
            DeleteMovieResponse response = DeleteMovieResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Movie deleted")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Movie not found"));
        }
    }

    @Override
    public void getMoviesByGenre(GetMoviesByGenreRequest request, StreamObserver<GetAllMoviesResponse> responseObserver) {
        List<Movie> movies = movieRepository.findByGenresContaining(request.getGenre());
        GetAllMoviesResponse response = GetAllMoviesResponse.newBuilder()
                .addAllMovies(movies.stream().map(this::toMovieResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMoviesByDirector(GetMoviesByDirectorRequest request, StreamObserver<GetAllMoviesResponse> responseObserver) {
        List<Movie> movies = movieRepository.findByDirectorsContaining(request.getDirector());
        GetAllMoviesResponse response = GetAllMoviesResponse.newBuilder()
                .addAllMovies(movies.stream().map(this::toMovieResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMoviesByActor(GetMoviesByActorRequest request, StreamObserver<GetAllMoviesResponse> responseObserver) {
        List<Movie> movies = movieRepository.findByActorsContaining(request.getActor());
        GetAllMoviesResponse response = GetAllMoviesResponse.newBuilder()
                .addAllMovies(movies.stream().map(this::toMovieResponse).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return MovieResponse.newBuilder()
                .setId(movie.getId())
                .setTitle(movie.getTitle())
                .addAllGenres(movie.getGenres())
                .addAllDirectors(movie.getDirectors())
                .addAllActors(movie.getActors())
                .setRunTime(movie.getRunTime())
                .setReleaseDate(movie.getReleaseDate().toString())
                .setRating(movie.getRating() != null ? movie.getRating() : 0.0)
                .setDescription(movie.getDescription() != null ? movie.getDescription() : "")
                .setPosterUrl(movie.getPosterUrl() != null ? movie.getPosterUrl() : "")
                .build();
    }
}