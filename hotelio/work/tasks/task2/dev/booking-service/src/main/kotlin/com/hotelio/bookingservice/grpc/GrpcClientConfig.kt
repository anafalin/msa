package com.hotelio.bookingservice.grpc

import com.example.grpc.proto.AppUserServiceGrpc
import com.example.grpc.proto.HotelServiceGrpc
import com.example.grpc.proto.PromoCodeServiceGrpc
import com.example.grpc.proto.ReviewServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcClientConfig(
    @Value("\${monolith.host}") private val host: String,
    @Value("\${monolith.port}") private val port: Int
) {
    private lateinit var channel: ManagedChannel

    @Bean
    fun monolithChannel(): ManagedChannel {
        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()
        return channel
    }

    @Bean
    fun appUserServiceStub(channel: ManagedChannel): AppUserServiceGrpc.AppUserServiceBlockingStub {
        return AppUserServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun promoCodeServiceStub(channel: ManagedChannel): PromoCodeServiceGrpc.PromoCodeServiceBlockingStub {
        return PromoCodeServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun hotelServiceStub(channel: ManagedChannel): HotelServiceGrpc.HotelServiceBlockingStub {
        return HotelServiceGrpc.newBlockingStub(channel)
    }

    @Bean
    fun reviewServiceStub(channel: ManagedChannel): ReviewServiceGrpc.ReviewServiceBlockingStub {
        return ReviewServiceGrpc.newBlockingStub(channel)
    }

    @PreDestroy
    fun shutdown() {
        if (::channel.isInitialized) {
            channel.shutdown()
        }
    }
}