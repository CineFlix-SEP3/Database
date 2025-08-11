package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sep3.cineflix.db_service.Entities.Movie;
import sep3.cineflix.db_service.Entities.Review;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Repositories.MovieRepository;
import sep3.cineflix.db_service.Repositories.ReviewRepository;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.grpc.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Captor
    private ArgumentCaptor<ReviewResponse> reviewResponseCaptor;

    @Captor
    private ArgumentCaptor<DeleteReviewResponse> deleteReviewCaptor;

    private Movie sampleMovie;
    private User sampleUser;
    private Review sampleReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleMovie = new Movie();
        sampleMovie.setId(1L);
        sampleUser = new User();
        sampleUser.setId(2L);
        sampleReview = new Review();
        sampleReview.setId(3L);
        sampleReview.setMovie(sampleMovie);
        sampleReview.setUser(sampleUser);
        sampleReview.setText("Great movie");
        sampleReview.setRating(5);
    }

    @Test
    void createReview_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(reviewRepository.save(any())).thenReturn(sampleReview);

        StreamObserver<ReviewResponse> observer = mock(StreamObserver.class);
        reviewService.createReview(CreateReviewRequest.newBuilder()
                .setMovieId(1L)
                .setUserId(2L)
                .setText("Great movie")
                .setRating(5)
                .build(), observer);

        verify(observer).onNext(reviewResponseCaptor.capture());
        verify(observer).onCompleted();
        assertEquals("Great movie", reviewResponseCaptor.getValue().getText());
    }

    @Test
    void createReview_MovieOrUserNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        StreamObserver<ReviewResponse> observer = mock(StreamObserver.class);
        reviewService.createReview(CreateReviewRequest.newBuilder()
                .setMovieId(1L).setUserId(2L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void getReviewsByMovie_ReturnsList() {
        when(reviewRepository.findByMovieId(1L)).thenReturn(List.of(sampleReview));

        StreamObserver<GetAllReviewsResponse> observer = mock(StreamObserver.class);
        reviewService.getReviewsByMovie(GetReviewsByMovieRequest.newBuilder()
                .setMovieId(1L).build(), observer);

        verify(observer).onNext(any(GetAllReviewsResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void updateReview_Success() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(any())).thenReturn(sampleReview);

        StreamObserver<ReviewResponse> observer = mock(StreamObserver.class);
        reviewService.updateReview(UpdateReviewRequest.newBuilder()
                .setId(3L).setText("Updated").setRating(4).build(), observer);

        verify(observer).onNext(reviewResponseCaptor.capture());
        verify(observer).onCompleted();
        assertEquals("Updated", reviewResponseCaptor.getValue().getText());
    }

    @Test
    void updateReview_NotFound() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.empty());

        StreamObserver<ReviewResponse> observer = mock(StreamObserver.class);
        reviewService.updateReview(UpdateReviewRequest.newBuilder()
                .setId(3L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void deleteReview_Success() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(sampleReview));

        StreamObserver<DeleteReviewResponse> observer = mock(StreamObserver.class);
        reviewService.deleteReview(DeleteReviewRequest.newBuilder()
                .setId(3L).build(), observer);

        verify(observer).onNext(deleteReviewCaptor.capture());
        assertTrue(deleteReviewCaptor.getValue().getSuccess());
        verify(observer).onCompleted();
    }

    @Test
    void deleteReview_NotFound() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.empty());

        StreamObserver<DeleteReviewResponse> observer = mock(StreamObserver.class);
        reviewService.deleteReview(DeleteReviewRequest.newBuilder()
                .setId(3L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }

    @Test
    void getReviewById_Found() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(sampleReview));

        StreamObserver<GetReviewByIdResponse> observer = mock(StreamObserver.class);
        reviewService.getReviewById(GetReviewByIdRequest.newBuilder()
                .setId(3L).build(), observer);

        verify(observer).onNext(any(GetReviewByIdResponse.class));
        verify(observer).onCompleted();
    }

    @Test
    void getReviewById_NotFound() {
        when(reviewRepository.findById(3L)).thenReturn(Optional.empty());

        StreamObserver<GetReviewByIdResponse> observer = mock(StreamObserver.class);
        reviewService.getReviewById(GetReviewByIdRequest.newBuilder()
                .setId(3L).build(), observer);

        verify(observer).onError(any(RuntimeException.class));
    }
}
