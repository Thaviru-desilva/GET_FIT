package com.gym.management.dto;

import com.gym.management.model.WorkoutFocus;

import java.time.LocalTime;

public class WorkoutSlotAvailabilityDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlotLabel;
    private WorkoutFocus workoutFocus;
    private int capacityLimit;
    private int bookedCount;
    private int remainingSpots;
    private boolean isFull;
    private boolean isUserBooked;
    private boolean isPast;

    public WorkoutSlotAvailabilityDto() {
    }

    public WorkoutSlotAvailabilityDto(LocalTime startTime,
                                      LocalTime endTime,
                                      String timeSlotLabel,
                                      WorkoutFocus workoutFocus,
                                      int capacityLimit,
                                      int bookedCount,
                                      int remainingSpots,
                                      boolean isFull,
                                      boolean isUserBooked,
                                      boolean isPast) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeSlotLabel = timeSlotLabel;
        this.workoutFocus = workoutFocus;
        this.capacityLimit = capacityLimit;
        this.bookedCount = bookedCount;
        this.remainingSpots = remainingSpots;
        this.isFull = isFull;
        this.isUserBooked = isUserBooked;
        this.isPast = isPast;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeSlotLabel() {
        return timeSlotLabel;
    }

    public void setTimeSlotLabel(String timeSlotLabel) {
        this.timeSlotLabel = timeSlotLabel;
    }

    public WorkoutFocus getWorkoutFocus() {
        return workoutFocus;
    }

    public void setWorkoutFocus(WorkoutFocus workoutFocus) {
        this.workoutFocus = workoutFocus;
    }

    public int getCapacityLimit() {
        return capacityLimit;
    }

    public void setCapacityLimit(int capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(int bookedCount) {
        this.bookedCount = bookedCount;
    }

    public int getRemainingSpots() {
        return remainingSpots;
    }

    public void setRemainingSpots(int remainingSpots) {
        this.remainingSpots = remainingSpots;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public boolean isUserBooked() {
        return isUserBooked;
    }

    public void setUserBooked(boolean userBooked) {
        isUserBooked = userBooked;
    }

    public boolean isPast() {
        return isPast;
    }

    public void setPast(boolean past) {
        isPast = past;
    }
}
