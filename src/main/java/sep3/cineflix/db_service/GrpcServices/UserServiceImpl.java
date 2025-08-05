package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Entities.UserRole;
import sep3.cineflix.db_service.Repositories.ReviewRepository;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.grpc.*;

import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        if (userRepository.existsByEmail(request.getEmail())) {
            responseObserver.onError(new RuntimeException("Email already in use"));
            return;
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            responseObserver.onError(new RuntimeException("Username already in use"));
            return;
        }
        User user = new User(
                null,
                request.getUsername(),
                request.getEmail(),
                request.getHashedPassword(),
                UserRole.valueOf(request.getUserRole().toUpperCase())
        );
        User savedUser = userRepository.save(user);
        responseObserver.onNext(toUserResponse(savedUser));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        userOpt.ifPresentOrElse(
                user -> {
                    responseObserver.onNext(toUserResponse(user));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(new RuntimeException("User not found"))
        );
    }

    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        userOpt.ifPresentOrElse(
                user -> {
                    responseObserver.onNext(toUserResponse(user));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(new RuntimeException("User not found"))
        );
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findById(request.getId());
        userOpt.ifPresentOrElse(
                user -> {
                    responseObserver.onNext(toUserResponse(user));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(new RuntimeException("User not found"))
        );
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findById(request.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                responseObserver.onError(new RuntimeException("Email already in use"));
                return;
            }
            if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
                responseObserver.onError(new RuntimeException("Username already in use"));
                return;
            }

            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setHashedPassword(request.getHashedPassword());
            user.setUserRole(UserRole.valueOf(request.getUserRole().toUpperCase()));
            User updatedUser = userRepository.save(user);
            responseObserver.onNext(toUserResponse(updatedUser));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("User not found"));
        }
    }


    @Override
    @Transactional
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        if (userRepository.existsById(request.getId())) {
            reviewRepository.deleteByUserId(request.getId()); // Delete reviews first
            userRepository.deleteById(request.getId());
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User deleted")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new RuntimeException("User not found"));
        }
    }

    @Override
    public void getAllUsers(GetAllUsersRequest request, StreamObserver<GetAllUsersResponse> responseObserver) {
        var userResponses = userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        GetAllUsersResponse response = GetAllUsersResponse.newBuilder()
                .addAllUsers(userResponses)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private UserResponse toUserResponse(User user) {
        return UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setHashedPassword(user.getHashedPassword())
                .setUserRole(user.getUserRole().name())
                .build();
    }
}