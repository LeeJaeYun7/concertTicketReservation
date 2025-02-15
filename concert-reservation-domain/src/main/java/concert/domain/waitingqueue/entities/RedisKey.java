package concert.domain.waitingqueue.entities;

public enum RedisKey {

    WAITING_QUEUE("waitingQueue"),
    ACTIVE_QUEUE("activeQueue"),
    ACTIVATED_TOKENS("activatedTokens"),
    TOKEN_SESSION_ID("tokenSessionId"),
    WAITING_QUEUE_STATUS("waitingQueueStatusKey"),
    WAITING_QUEUE_STATUS_ACTIVE("active"),
    ACTIVE_QUEUE_LOCK("activeQueueLock"),
    TOKEN_PUB_SUB_CHANNEL("tokenChannel"),
    WAITING_QUEUE_STATUS_PUB_SUB_CHANNEL("waitingQueueStatusChannel"),

    WAITING_QUEUE_STATUS_KEY("waitingQueueStatusKey");

    private final String key;

    RedisKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
