package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sep3.cineflix.db_service.Entities.Movie;
import sep3.cineflix.db_service.Repositories.MovieRepository;
import sep3.cineflix.grpc.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Captor
    private ArgumentCaptor<MovieResponse> movieResponseCaptor;

    @Captor
    private ArgumentCaptor<DeleteMovieResponse> deleteResponseCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Movie sampleMovie() {
        return Movie.builder()
                .id(1L)
                .title("Inception")
                .genres(Set.of("Sci-Fi"))
                .directors(Set.of("Christopher Nolan"))
                .actors(Set.of("Leonardo DiCaprio"))
                .runTime(148)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .rating(8.8)
                .description("Mind-bending thriller")
                .posterUrl("url")
                .build();
    }

    @Test
    void createMovie_Success() {
        CreateMovieRequest request = CreateMovieRequest.newBuilder()
                .setTitle("Inception")
                .addGenres("Sci-Fi")
                .addDirectors("Christopher Nolan")
                .addActors("Leonardo DiCaprio")
                .setRunTime(148)
                .setReleaseDate("2010-07-16")
                .setDescription("Mind-bending thriller")
                .setPosterUrl("url")
                .build();

        when(movieRepository.existsByTitle("Inception")).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> {
            Movie m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);
        movieService.createMovie(request, observer);

        verify(observer).onNext(movieResponseCaptor.capture());
        verify(observer).onCompleted();

        assertEquals("Inception", movieResponseCaptor.getValue().getTitle());
        assertEquals(1L, movieResponseCaptor.getValue().getId());
    }

    @Test
    void createMovie_TitleExists_Error() {
        CreateMovieRequest request = CreateMovieRequest.newBuilder().setTitle("Inception").build();
        when(movieRepository.existsByTitle("Inception")).thenReturn(true);

        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);
        movieService.createMovie(request, observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void getMovieById_Found() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie()));

        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);
        movieService.getMovieById(GetMovieByIdRequest.newBuilder().setId(1L).build(), observer);

        verify(observer).onNext(movieResponseCaptor.capture());
        verify(observer).onCompleted();
        assertEquals("Inception", movieResponseCaptor.getValue().getTitle());
    }

    @Test
    void getMovieById_NotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);

        movieService.getMovieById(GetMovieByIdRequest.newBuilder().setId(1L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void updateMovie_Success() {
        Movie existing = sampleMovie();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.existsByTitle("New Title")).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenReturn(existing);

        UpdateMovieRequest request = UpdateMovieRequest.newBuilder()
                .setId(1L)
                .setTitle("New Title")
                .addGenres("Sci-Fi")
                .addDirectors("Nolan")
                .addActors("Leo")
                .setRunTime(150)
                .setReleaseDate("2010-07-16")
                .setDescription("Updated")
                .setPosterUrl("newUrl")
                .build();

        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);
        movieService.updateMovie(request, observer);

        verify(observer).onNext(movieResponseCaptor.capture());
        verify(observer).onCompleted();
        assertEquals("New Title", movieResponseCaptor.getValue().getTitle());
    }

    @Test
    void updateMovie_NotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        StreamObserver<MovieResponse> observer = mock(StreamObserver.class);

        movieService.updateMovie(UpdateMovieRequest.newBuilder().setId(1L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void deleteMovie_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);

        StreamObserver<DeleteMovieResponse> observer = mock(StreamObserver.class);
        movieService.deleteMovie(DeleteMovieRequest.newBuilder().setId(1L).build(), observer);

        verify(observer).onNext(deleteResponseCaptor.capture());
        verify(observer).onCompleted();
        assertTrue(deleteResponseCaptor.getValue().getSuccess());
    }

    @Test
    void deleteMovie_NotFound() {
        when(movieRepository.existsById(1L)).thenReturn(false);

        StreamObserver<DeleteMovieResponse> observer = mock(StreamObserver.class);
        movieService.deleteMovie(DeleteMovieRequest.newBuilder().setId(1L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void getMoviesByGenre_ReturnsList() {
        when(movieRepository.findByGenresContaining("Sci-Fi")).thenReturn(List.of(sampleMovie()));

        StreamObserver<GetAllMoviesResponse> observer = mock(StreamObserver.class);
        movieService.getMoviesByGenre(GetMoviesByGenreRequest.newBuilder().setGenre("Sci-Fi").build(), observer);

        verify(observer).onNext(any(GetAllMoviesResponse.class));
        verify(observer).onCompleted();
    }
}
