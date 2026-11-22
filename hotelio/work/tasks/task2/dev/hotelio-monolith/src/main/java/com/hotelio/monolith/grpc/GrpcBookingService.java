package com.hotelio.monolith.grpc;

import com.hotelio.monolith.entity.Booking;
import com.hotelio.monolith.repository.BookingRepository;
import com.hotelio.monolith.service.AppUserService;
import com.hotelio.monolith.service.BookingService;
import com.hotelio.monolith.service.HotelService;
import com.hotelio.monolith.service.PromoCodeService;
import com.hotelio.monolith.service.ReviewService;
import com.hotelio.proto.booking.BookingRequest;
import com.hotelio.proto.booking.BookingResponse;
import com.hotelio.proto.booking.BookingServiceGrpc;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcBookingService extends BookingService {
    private static final Logger logger = LoggerFactory.getLogger(GrpcBookingService.class);
    private final BookingServiceGrpc.BookingServiceBlockingStub stub;
    private final BookingRepository bookingRepository;

    public GrpcBookingService(BookingServiceGrpc.BookingServiceBlockingStub stub, BookingRepository bookingRepository) {
        super((BookingRepository) null, (PromoCodeService) null, (ReviewService) null, (AppUserService) null, (HotelService) null);
        logger.info("Using GRPC BookingService");
        this.stub = stub;
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> listAll(String userId) {
        if(userId == null) {
            return bookingRepository.findAll();
        }
        return bookingRepository.findByUserId(userId);
//        BookingListRequest request = BookingListRequest.newBuilder().setUserId(userId).build();
//        BookingListResponse response = this.stub.listBookings(request);
//        return response.getBookingsList().stream().map(this::toBooking).toList();
    }

    public Booking createBooking(String userId, String hotelId, String promoCode) {
        BookingRequest request = BookingRequest.newBuilder().setUserId(userId).setHotelId(hotelId).setPromoCode(Optional.ofNullable(promoCode).orElse("")).build();
        BookingResponse response = this.stub.createBooking(request);
        return this.toBooking(response);
    }

    private Booking toBooking(BookingResponse response) {
        Booking booking = new Booking();
        String idStr = response.getId();
        if (!idStr.isBlank()) {
            booking.setId(Long.parseLong(idStr));
        }

        booking.setUserId(response.getUserId());
        booking.setHotelId(response.getHotelId());
        booking.setPromoCode(response.getPromoCode());
        booking.setDiscountPercent(response.getDiscountPercent());
        booking.setPrice(response.getPrice());
        booking.setCreatedAt(Instant.parse(response.getCreatedAt()));
        return booking;
    }
}
