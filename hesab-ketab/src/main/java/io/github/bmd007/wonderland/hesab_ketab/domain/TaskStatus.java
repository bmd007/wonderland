package io.github.bmd007.wonderland.hesab_ketab.domain;

public enum TaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED;

    public String toDbValue() {
        return name().toLowerCase();
    }

    public static TaskStatus fromDbValue(String value) {
        return valueOf(value.toUpperCase());
    }
}
