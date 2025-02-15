package concert.application.waitingqueue.business.enums;

public enum QueueStatusTrigger {

    ACTIVATION_TRIGGER_TRAFFIC(1500L),
    DEACTIVATION_TRIGGER_TRAFFIC(300L),
    COOLDOWN_TIME(180_000L);  // 3분 (밀리초 단위)

    private final long value;

    QueueStatusTrigger(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
