package com.thirdchannel.eventsource

/**
 * @author steve pember
 */
public interface Aggregate {
    /**
     *
     * @return
     */
    UUID getId()

    /**
     *
     * @return
     */
    String getAggregateDescription()

    int getRevision()

    /**
     *
     * Internally maintains a list of uncommitted events
     *
     * @return a list of {@link Event}s that have not yet been persisted to the store
     */
    List<Event> getUncommittedEvents()

    /**
     *
     */
    void markEventsAsCommitted()

    /**
     * Similar to applyChange, but these events should not be considered as new/uncommitted
     *
     * @param events
     */
    void loadFromPastEvents(List<Event> events)

    /**
     * Changes, in this case are *new* {@see Event}
     *
     * @param event
     */
    void applyChange(Event event)





}