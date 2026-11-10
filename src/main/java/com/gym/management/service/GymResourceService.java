package com.gym.management.service;

import com.gym.management.model.GymResource;
import com.gym.management.model.Member;
import com.gym.management.model.ResourceBooking;
import com.gym.management.model.Trainer;
import com.gym.management.repository.GymResourceRepository;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.ResourceBookingRepository;
import com.gym.management.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GymResourceService {

    public static final int MAX_GYM_FLOOR_CAPACITY = 50;

    private final GymResourceRepository resourceRepository;
    private final ResourceBookingRepository bookingRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final AttendanceService attendanceService;

    public GymResourceService(GymResourceRepository resourceRepository,
                              ResourceBookingRepository bookingRepository,
                              MemberRepository memberRepository,
                              TrainerRepository trainerRepository,
                              AttendanceService attendanceService) {
        this.resourceRepository = resourceRepository;
        this.bookingRepository = bookingRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.attendanceService = attendanceService;
    }

    public List<GymResource> findAll() {
        return resourceRepository.findAll();
    }

    public Optional<GymResource> findById(Long id) {
        return resourceRepository.findById(id);
    }

    public GymResource save(GymResource resource) {
        return resourceRepository.save(resource);
    }

    public void deleteById(Long id) {
        resourceRepository.deleteById(id);
    }

    public List<GymResource> findByCategory(String category) {
        return resourceRepository.findByCategoryOrderByAvailableQuantityDesc(category);
    }

    public List<GymResource> findAvailableResources() {
        return resourceRepository.findByAvailableQuantityGreaterThan(0);
    }

    public ResourceBooking bookResource(Long resourceId,
                                        Long memberId,
                                        Long trainerId,
                                        LocalDate date,
                                        LocalTime startTime,
                                        LocalTime endTime,
                                        String purpose) {
        GymResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Gym Resource not found with ID: " + resourceId));

        if ("MAINTENANCE".equalsIgnoreCase(resource.getStatus())) {
            throw new IllegalStateException("Resource '" + resource.getName() + "' is currently under maintenance.");
        }

        if (startTime == null) startTime = LocalTime.now().withSecond(0).withNano(0);
        if (endTime == null) endTime = startTime.plusMinutes(60);
        if (date == null) date = LocalDate.now();

        boolean isToday = date.isEqual(LocalDate.now());

        if (isToday) {
            if (resource.getAvailableQuantity() <= 0) {
                throw new IllegalStateException("All units of '" + resource.getName() + "' are currently reserved or in use for today.");
            }
        } else {
            final LocalTime slotStart = startTime;
            final LocalTime slotEnd = endTime;
            long overlapping = bookingRepository.findByResourceIdAndBookingDateAndStatus(resourceId, date, "CONFIRMED")
                    .stream()
                    .filter(b -> slotStart.isBefore(b.getEndTime()) && slotEnd.isAfter(b.getStartTime()))
                    .count();
            if (overlapping >= resource.getTotalQuantity()) {
                throw new IllegalStateException("All " + resource.getTotalQuantity() + " units of '" + resource.getName() + "' are already booked for this time slot on " + date + ".");
            }
        }

        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId).orElse(null);
        }

        Trainer trainer = null;
        if (trainerId != null) {
            trainer = trainerRepository.findById(trainerId).orElse(null);
        }

        ResourceBooking booking = new ResourceBooking(resource, member, trainer, date, startTime, endTime, purpose);
        booking = bookingRepository.save(booking);

        // Adjust live floor inventory only for bookings scheduled for TODAY
        if (isToday) {
            resource.setAvailableQuantity(Math.max(0, resource.getAvailableQuantity() - 1));
            if (resource.getAvailableQuantity() == 0) {
                resource.setStatus("IN_USE");
            }
            resourceRepository.save(resource);
        }

        return booking;
    }

    public void cancelBooking(Long bookingId, Long requestingMemberId, Long requestingTrainerId, boolean isAdmin) {
        ResourceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with ID: " + bookingId));

        if (!isAdmin) {
            boolean isAuthorizedMember = requestingMemberId != null && booking.getMember() != null && booking.getMember().getId().equals(requestingMemberId);
            boolean isAuthorizedTrainer = requestingTrainerId != null && booking.getTrainer() != null && booking.getTrainer().getId().equals(requestTrainerIdOrNull(booking, requestingTrainerId));
            if (!isAuthorizedMember && !isAuthorizedTrainer) {
                throw new IllegalStateException("Unauthorized: You can only cancel your own equipment reservations.");
            }
        }

        if (!"CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            GymResource resource = booking.getResource();
            if (resource != null && booking.getBookingDate() != null && booking.getBookingDate().isEqual(LocalDate.now())) {
                resource.setAvailableQuantity(Math.min(resource.getTotalQuantity(), resource.getAvailableQuantity() + 1));
                if ("IN_USE".equalsIgnoreCase(resource.getStatus()) || "RESERVED".equalsIgnoreCase(resource.getStatus())) {
                    resource.setStatus("AVAILABLE");
                }
                resourceRepository.save(resource);
            }
        }
    }

    private Long requestTrainerIdOrNull(ResourceBooking booking, Long trainerId) {
        return booking.getTrainer() != null ? booking.getTrainer().getId() : null;
    }

    public void cancelBooking(Long bookingId) {
        cancelBooking(bookingId, null, null, true);
    }

    public void updateResourceStatus(Long resourceId, String newStatus) {
        resourceRepository.findById(resourceId).ifPresent(res -> {
            res.setStatus(newStatus);
            if ("MAINTENANCE".equalsIgnoreCase(newStatus)) {
                res.setAvailableQuantity(0);
            } else if ("AVAILABLE".equalsIgnoreCase(newStatus) && res.getAvailableQuantity() == 0) {
                res.setAvailableQuantity(res.getTotalQuantity());
            }
            resourceRepository.save(res);
        });
    }

    public List<ResourceBooking> getMemberBookings(Long memberId) {
        return bookingRepository.findByMemberIdOrderByBookingDateDescStartTimeDesc(memberId);
    }

    public List<ResourceBooking> getTrainerBookings(Long trainerId) {
        return bookingRepository.findByTrainerIdOrderByBookingDateDescStartTimeDesc(trainerId);
    }

    public List<ResourceBooking> getTodayBookings() {
        return bookingRepository.findByBookingDateOrderByStartTimeAsc(LocalDate.now());
    }

    // Facility Headcount Capacity Tracking
    public int getMaxFloorCapacity() {
        return MAX_GYM_FLOOR_CAPACITY;
    }

    public int getCurrentFloorOccupancy() {
        return (int) attendanceService.getTodayAttendance().stream()
                .filter(a -> a.getCheckOutTime() == null)
                .count();
    }

    public int getFloorOccupancyPercentage() {
        int current = getCurrentFloorOccupancy();
        return Math.min(100, (int) Math.round(((double) current / MAX_GYM_FLOOR_CAPACITY) * 100));
    }
}
