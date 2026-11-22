package com.hotelio.monolith.config;


import com.hotelio.monolith.repository.BookingRepository;
import com.hotelio.monolith.service.BookingService;
import com.hotelio.monolith.grpc.GrpcBookingService;
import com.hotelio.proto.booking.BookingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BookingServiceProxyConfig {
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = {"booking.service.external-host"}, matchIfMissing = false)
    public ManagedChannel grpcChannel(@Value("${booking.service.external-host}") String host, @Value("${booking.service.external-port}") int port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = {"booking.service.external-host"}, matchIfMissing = false)
    public BookingService grpcBookingService(ManagedChannel channel, BookingRepository bookingRepository) {
        return new GrpcBookingService(BookingServiceGrpc.newBlockingStub(channel), bookingRepository);
    }
}

