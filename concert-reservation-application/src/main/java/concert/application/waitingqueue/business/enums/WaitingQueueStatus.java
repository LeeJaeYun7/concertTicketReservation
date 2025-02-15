package concert.application.waitingqueue.business.enums;

public enum WaitingQueueStatus {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    WaitingQueueStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
