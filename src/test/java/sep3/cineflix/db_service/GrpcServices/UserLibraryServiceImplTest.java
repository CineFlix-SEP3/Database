package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sep3.cineflix.db_service.Entities.Movie;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Entities.UserFavorites;
import sep3.cineflix.db_service.Entities.UserWatchList;
import sep3.cineflix.db_service.Repositories.*;

import sep3.cineflix.grpc.userlibrary.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserLibraryServiceImplTest {

    @Mock
    private UserFavoritesRepository favoritesRepository;
    @Mock
    private UserWatchListRepository watchListRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private UserLibraryServiceImpl service;

    @Captor
    private ArgumentCaptor<AddFavoriteResponse> addFavCaptor;
    @Captor
    private ArgumentCaptor<AddWatchListResponse> addWatchCaptor;

    private User user;
    private Movie movie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        movie = new Movie();
        movie.setId(2L);
    }

    @Test
    void addFavorite_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(favoritesRepository.existsByUserIdAndMovieId(1L, 2L)).thenReturn(false);

        StreamObserver<AddFavoriteResponse> observer = mock(StreamObserver.class);
        service.addFavorite(AddFavoriteRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addFavCaptor.capture());
        verify(observer).onCompleted();
        assertTrue(addFavCaptor.getValue().getSuccess());
    }

    @Test
    void addFavorite_UserOrMovieNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        StreamObserver<AddFavoriteResponse> observer = mock(StreamObserver.class);
        service.addFavorite(AddFavoriteRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addFavCaptor.capture());
        assertFalse(addFavCaptor.getValue().getSuccess());
    }

    @Test
    void addFavorite_AlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(favoritesRepository.existsByUserIdAndMovieId(1L, 2L)).thenReturn(true);

        StreamObserver<AddFavoriteResponse> observer = mock(StreamObserver.class);
        service.addFavorite(AddFavoriteRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addFavCaptor.capture());
        assertFalse(addFavCaptor.getValue().getSuccess());
    }

    @Test
    void getFavorites_ReturnsList() {
        UserFavorites fav = new UserFavorites(1L, user, movie, LocalDateTime.now());
        when(favoritesRepository.findByUserId(1L)).thenReturn(List.of(fav));

        StreamObserver<GetFavoritesResponse> observer = mock(StreamObserver.class);
        service.getFavorites(GetFavoritesRequest.newBuilder().setUserId(1L).build(), observer);

        verify(observer).onNext(any(GetFavoritesResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void removeFavorite_Success() {
        StreamObserver<RemoveFavoriteResponse> observer = mock(StreamObserver.class);
        service.removeFavorite(RemoveFavoriteRequest.newBuilder().setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(any(RemoveFavoriteResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void addWatchList_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(watchListRepository.existsByUserIdAndMovieId(1L, 2L)).thenReturn(false);

        StreamObserver<AddWatchListResponse> observer = mock(StreamObserver.class);
        service.addWatchList(AddWatchListRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addWatchCaptor.capture());
        verify(observer).onCompleted();
        assertTrue(addWatchCaptor.getValue().getSuccess());
    }

    @Test
    void addWatchList_UserOrMovieNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        StreamObserver<AddWatchListResponse> observer = mock(StreamObserver.class);
        service.addWatchList(AddWatchListRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addWatchCaptor.capture());
        assertFalse(addWatchCaptor.getValue().getSuccess());
    }

    @Test
    void addWatchList_AlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie));
        when(watchListRepository.existsByUserIdAndMovieId(1L, 2L)).thenReturn(true);

        StreamObserver<AddWatchListResponse> observer = mock(StreamObserver.class);
        service.addWatchList(AddWatchListRequest.newBuilder()
                .setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(addWatchCaptor.capture());
        assertFalse(addWatchCaptor.getValue().getSuccess());
    }

    @Test
    void getWatchList_ReturnsList() {
        UserWatchList wl = new UserWatchList(1L, user, movie, LocalDateTime.now());
        when(watchListRepository.findByUserId(1L)).thenReturn(List.of(wl));

        StreamObserver<GetWatchListResponse> observer = mock(StreamObserver.class);
        service.getWatchList(GetWatchListRequest.newBuilder().setUserId(1L).build(), observer);

        verify(observer).onNext(any(GetWatchListResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void removeWatchList_Success() {
        StreamObserver<RemoveWatchListResponse> observer = mock(StreamObserver.class);
        service.removeWatchList(RemoveWatchListRequest.newBuilder().setUserId(1L).setMovieId(2L).build(), observer);

        verify(observer).onNext(any(RemoveWatchListResponse.class));
        verify(observer).onCompleted();
    }
}
