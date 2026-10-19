package com.gym.management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "gym_resources")
public class GymResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Resource name is required")
    @Column(nullable = false)
    private String name; // e.g. "Olympic Squat Rack #1", "Commercial Treadmill T70"

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category; // CARDIO, STRENGTH, FUNCTIONAL, STUDIO, RECOVERY, LOCKER

    private String location; // e.g. "Free Weight Zone", "Cardio Deck", "Studio A"

    @NotNull
    @Min(1)
    private Integer totalQuantity = 1;

    @NotNull
    @Min(0)
    private Integer availableQuantity = 1;

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, IN_USE, RESERVED, MAINTENANCE

    @Column(length = 500)
    private String description;

    private LocalDateTime lastMaintenanceDate = LocalDateTime.now().minusWeeks(2);

    public GymResource() {
    }

    public GymResource(String name, String category, String location, Integer totalQuantity, Integer availableQuantity, String status, String description) {
        this.name = name;
        this.category = category;
        this.location = location;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 1;
        this.availableQuantity = availableQuantity != null ? availableQuantity : totalQuantity;
        this.status = status != null ? status : "AVAILABLE";
        this.description = description;
        this.lastMaintenanceDate = LocalDateTime.now().minusWeeks(2);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(this.status) && availableQuantity > 0;
    }

    public int getUsagePercentage() {
        if (totalQuantity == null || totalQuantity <= 0) return 0;
        int inUse = totalQuantity - (availableQuantity != null ? availableQuantity : 0);
        return Math.min(100, Math.max(0, (int) Math.round((double) inUse / totalQuantity * 100)));
    }

    public String getStatusBadgeClass() {
        if ("AVAILABLE".equalsIgnoreCase(status)) return "bg-success";
        if ("IN_USE".equalsIgnoreCase(status)) return "bg-primary";
        if ("RESERVED".equalsIgnoreCase(status)) return "bg-warning text-dark";
        if ("MAINTENANCE".equalsIgnoreCase(status)) return "bg-danger";
        return "bg-secondary";
    }

    public String getCategoryIcon() {
        if ("CARDIO".equalsIgnoreCase(category)) return "bi-heart-pulse-fill";
        if ("STRENGTH".equalsIgnoreCase(category)) return "bi-lightning-charge-fill";
        if ("FUNCTIONAL".equalsIgnoreCase(category)) return "bi-diagram-3-fill";
        if ("STUDIO".equalsIgnoreCase(category)) return "bi-door-open-fill";
        if ("RECOVERY".equalsIgnoreCase(category)) return "bi-water";
        if ("LOCKER".equalsIgnoreCase(category)) return "bi-lock-fill";
        return "bi-box-seam";
    }
}
