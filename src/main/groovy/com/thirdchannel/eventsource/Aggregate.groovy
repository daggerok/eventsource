package com.thirdchannel.eventsource

/**
 * @author steve pember
 */
interface Aggregate {
    UUID getId()
    void setId(UUID id)

    int getRevision()
    void setRevision(int revision)

    /**
     * Maintains uncommitted events
     *
     * @return {@link Event}s that have not yet been persisted to the store
     */
    List<? extends Event> getUncommittedEvents()
    void setUncommittedEvents(List<? extends Event> events)

    void markEventsAsCommitted()

    /**
     * Similar to applyChange, but these events should not be considered as new/uncommitted
     */
    void loadFromPastEvents(List<? extends Event> events)

    /**
     * Changes, in this case are *new* {@see Event}
     */
    void applyChange(Event event)

    void applyChanges(List<? extends Event> events)
}
