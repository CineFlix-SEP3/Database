package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sep3.cineflix.db_service.Entities.*;
import sep3.cineflix.db_service.Repositories.*;
import sep3.cineflix.grpc.*;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class ReviewServiceImpl extends ReviewServiceGrpc.ReviewServiceImplBase {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, MovieRepository movieRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void createReview(CreateReviewRequest request, StreamObserver<ReviewResponse> responseObserver) {
        Optional<Movie> movieOpt = movieRepository.findById(request.getMovieId());
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (movieOpt.isEmpty() || userOpt.isEmpty()) {
            responseObserver.onError(new RuntimeException("Movie or User not found"));
            return;
        }
        Review review = new Review();
        review.setMovie(movieOpt.get());
        review.setUser(userOpt.get());
        review.setText(request.getText());
        review.setRating(request.getRating());
        Review saved = reviewRepository.save(review);
        updateMovieRating(saved.getMovie());
        responseObserver.onNext(toReviewResponse(saved));
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void getReviewsByMovie(GetReviewsByMovieRequest request, StreamObserver<GetAllReviewsResponse> responseObserver) {
        var reviews = reviewRepository.findByMovieId(request.getMovieId())
                .stream().map(this::toReviewResponse).collect(Collectors.toList());
        GetAllReviewsResponse response = GetAllReviewsResponse.newBuilder().addAllReviews(reviews).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional(readOnly = true)
    public void getReviewsByUser(GetReviewsByUserRequest request, StreamObserver<GetAllReviewsResponse> responseObserver) {
        var reviews = reviewRepository.findByUserId(request.getUserId())
                .stream().map(this::toReviewResponse).collect(Collectors.toList());
        GetAllReviewsResponse response = GetAllReviewsResponse.newBuilder().addAllReviews(reviews).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateReview(UpdateReviewRequest request, StreamObserver<ReviewResponse> responseObserver) {
        Optional<Review> reviewOpt = reviewRepository.findById(request.getId());
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            review.setText(request.getText());
            review.setRating(request.getRating());
            Review updated = reviewRepository.save(review);
            updateMovieRating(updated.getMovie());
            responseObserver.onNext(toReviewResponse(updated));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Review not found"));
        }
    }

    @Override
    @Transactional
    public void deleteReview(DeleteReviewRequest request, StreamObserver<DeleteReviewResponse> responseObserver) {
        Optional<Review> reviewOpt = reviewRepository.findById(request.getId());
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            Movie movie = review.getMovie();
            reviewRepository.deleteById(request.getId());
            updateMovieRating(movie);
            DeleteReviewResponse response = DeleteReviewResponse.newBuilder().setSuccess(true).setMessage("Review deleted").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Review not found"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void getReviewById(GetReviewByIdRequest request, StreamObserver<GetReviewByIdResponse> responseObserver) {
        Optional<Review> reviewOpt = reviewRepository.findById(request.getId());
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            GetReviewByIdResponse response = GetReviewByIdResponse.newBuilder()
                    .setId(review.getId())
                    .setMovieId(review.getMovie().getId())
                    .setUserId(review.getUser().getId())
                    .setText(review.getText())
                    .setRating(review.getRating())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("Review not found"));
        }
    }

    @Transactional(readOnly = true)
    private void updateMovieRating(Movie movie) {
        List<Review> reviews = reviewRepository.findByMovieId(movie.getId());
        double avg = reviews.isEmpty() ? 0.0 :
                reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
        movie.setRating(avg);
        movieRepository.save(movie);
    }

    private ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.newBuilder()
                .setId(review.getId())
                .setMovieId(review.getMovie().getId())
                .setUserId(review.getUser().getId())
                .setText(review.getText())
                .setRating(review.getRating())
                .build();
    }
}