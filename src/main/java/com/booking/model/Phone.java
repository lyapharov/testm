package com.booking.model;

public enum Phone {
    SAMSUNG_GALAXY_S9((short) 1),
    SAMSUNG_GALAXY_S8((short) 2),
    MOTOROLA_NEXUS_6((short) 3),
    ONEPLUS_9((short) 4),
    APPLE_IPHONE_13((short) 5),
    APPLE_IPHONE_12((short) 6),
    APPLE_IPHONE_11((short) 7),
    APPLE_IPHONE_X((short) 8),
    NOKIA_3310((short) 9);

    private final short id;

    Phone(short id) {
        this.id = id;
    }

    public short getId() {
        return id;
    }
}
