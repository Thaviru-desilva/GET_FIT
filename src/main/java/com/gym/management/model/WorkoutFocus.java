package com.gym.management.model;

public enum WorkoutFocus {
    LEGS("Legs & Lower Body", "Squat Racks, Leg Press, Hack Squat, Leg Extension/Curl", 6, "bg-danger", "bi-lightning-charge-fill"),
    PUSH("Chest & Push", "Flat/Incline Benches, Cable Crossovers, Overhead Press, Dips", 8, "bg-primary", "bi-arrow-up-right-circle-fill"),
    PULL("Back & Pull", "Deadlift Platforms, Lat Pulldowns, Seated Cable Rows, Pull-Up Station", 8, "bg-success", "bi-arrow-down-left-circle-fill"),
    CARDIO_FUNCTIONAL("Cardio & Functional Turf", "Commercial Treadmills, Concept2 Rowers, Sled Track, Assault Bikes", 10, "bg-warning text-dark", "bi-heart-pulse-fill"),
    FULL_BODY("Full Body & Conditioning", "Kettlebells, Dumbbell Racks, Plyo Boxes, Core Station", 10, "bg-info text-dark", "bi-activity");

    private final String displayName;
    private final String equipmentDescription;
    private final int capacity;
    private final String badgeClass;
    private final String icon;

    WorkoutFocus(String displayName, String equipmentDescription, int capacity, String badgeClass, String icon) {
        this.displayName = displayName;
        this.equipmentDescription = equipmentDescription;
        this.capacity = capacity;
        this.badgeClass = badgeClass;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEquipmentDescription() {
        return equipmentDescription;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getIcon() {
        return icon;
    }
}
