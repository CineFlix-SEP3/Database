package sep3.cineflix.db_service.GrpcServices;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import sep3.cineflix.db_service.Entities.User;
import sep3.cineflix.db_service.Entities.UserRole;
import sep3.cineflix.db_service.Repositories.ReviewRepository;
import sep3.cineflix.db_service.Repositories.UserRepository;
import sep3.cineflix.grpc.*;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private UserServiceImpl service;

    @Captor
    private ArgumentCaptor<UserResponse> userResponseCaptor;
    @Captor
    private ArgumentCaptor<DeleteUserResponse> deleteUserCaptor;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User(1L, "john_doe", "john@example.com", "hashedPass", UserRole.USER);
    }

    @Test
    void createUser_Success() {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setUsername("john_doe")
                .setEmail("john@example.com")
                .setHashedPassword("hashedPass")
                .setUserRole("USER")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        StreamObserver<UserResponse> observer = mock(StreamObserver.class);
        service.createUser(request, observer);

        verify(observer).onNext(userResponseCaptor.capture());
        verify(observer).onCompleted();
        assertEquals("john
