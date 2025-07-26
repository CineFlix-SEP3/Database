package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Entities.UserRole;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.grpc.*;

import java.util.Optional;
import java.util.stream.Collectors;

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
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        if (userRepository.existsById(request.getId())) {
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