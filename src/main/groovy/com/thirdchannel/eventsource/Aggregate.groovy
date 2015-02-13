package com.thirdchannel.eventsource

/**
 * @author steve pember
 */
interface Aggregate {
    UUID getId()

    String getAggregateDescription()

    int getRevision()

    /**
     * Internally maintains a list of uncommitted events
     *
     * @return {@link Event}s that have not yet been persisted to the store
     */
    List<Event> getUncommittedEvents()

    void markEventsAsCommitted()

    /**
     * Similar to applyChange, but these events should not be considered as new/uncommitted
     */
    void loadFromPastEvents(List<Event> events)

    /**
     * Changes, in this case are *new* {@see Event}
     */
    void applyChange(Event event)
}
