package com.hotelio.bookingservice.service

import com.example.grpc.proto.AppUserServiceGrpc
import com.example.grpc.proto.UserResponse
import com.example.grpc.proto.userIdRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AppUserService(
    private val userStub: AppUserServiceGrpc.AppUserServiceBlockingStub,
) {
    private val log = LoggerFactory.getLogger(AppUserService::class.java)

    fun isUserActive(userId: String): Boolean {
        return fetchUser(userId)?.active ?: false
    }

    fun isUserBlacklisted(userId: String): Boolean {
        return fetchUser(userId)?.blacklisted ?: true
    }

    fun getUserStatus(userId: String): Optional<String> {
        val status = fetchUser(userId)?.status
        return if (status.isNullOrBlank()) Optional.empty() else Optional.of(status)
    }

    private fun fetchUser(userId: String): UserResponse? {
        val request = userIdRequest { this.userId = userId }

        return try {
            userStub.getUserById(request)
        } catch (e: StatusRuntimeException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    log.warn("User {} not found in monolith via gRPC", userId)
                    null
                }
                else -> {
                    log.error("gRPC error while fetching user {}: {}", userId, e.status, e)
                    throw IllegalStateException("Failed to fetch user info from monolith", e)
                }
            }
        }
    }
}
