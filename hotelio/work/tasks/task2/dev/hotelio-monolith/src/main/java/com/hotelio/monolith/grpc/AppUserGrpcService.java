package com.hotelio.monolith.grpc;

import com.example.grpc.proto.AppUserServiceGrpc;
import com.example.grpc.proto.UserIdRequest;
import com.example.grpc.proto.UserResponse;
import com.hotelio.monolith.entity.AppUser;
import com.hotelio.monolith.repository.AppUserRepository;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInternalError;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInvalidArgument;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseNotFound;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseOk;

@GrpcService
public class AppUserGrpcService extends AppUserServiceGrpc.AppUserServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AppUserGrpcService.class);

    private final AppUserRepository repository;

    public AppUserGrpcService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getUserById(UserIdRequest request, StreamObserver<UserResponse> responseObserver) {
        if (request.getUserId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "userId: must not be blank");
            return;
        }

        try {
            String userId = request.getUserId();
            Optional<AppUser> userOptional = repository.findById(userId);

            if (userOptional.isEmpty()) {
                createResponseNotFound(responseObserver, "User not found with id: " + userId);
                return;
            }

            AppUser user = userOptional.get();
            UserResponse response = UserResponse.newBuilder()
                .setId(user.getId())
                .setStatus(user.getStatus() != null ? user.getStatus() : "")
                .setBlacklisted(user.isBlacklisted())
                .setActive(user.isActive())
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error("Failed to get user by id {}", request.getUserId(), e);
            createResponseInternalError(responseObserver, "Internal error while fetching user", e);
        }
    }
}