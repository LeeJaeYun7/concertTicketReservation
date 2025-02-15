package concert.application.waitingqueue.business.enums;

public enum QueueStatus {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    QueueStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
