package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Entities.UserRole;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.grpc.CreateUserRequest;
import sep3.cineflix.grpc.GetUserByEmailRequest;
import sep3.cineflix.grpc.GetUserByUsernameRequest;
import sep3.cineflix.grpc.UserResponse;
import sep3.cineflix.grpc.UserServiceGrpc;

import java.util.Optional;

@Service
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        User user = new User(
                null,
                request.getUsername(),
                request.getEmail(),
                request.getHashedPassword(),
                UserRole.valueOf(request.getUserRole())
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