package com.thirdchannel.eventsource

/**
 * @author steve
 *
 * DAO Service for loading and persisting {@link Aggregate}s.
 *
 */
public interface AggregateService {

    void setEventService(EventService eventService)
    EventService getEventService()



    Aggregate get(UUID id)
    /**
     * Used to grab the current revision of the aggregate, ideally checked before saving events
     * to ensure that items are not placed out of order
     *
     * @param id
     * @return
     */
    //Integer getCurrentRevision(UUID id)
    Aggregate findAllByIdsInList(List<UUID> ids)

    /**
     * Should persist the aggregate & its events within a transaction.
     * Use the expectedRevision to ensure that we can only update records that have no not been modified since, then
     * save the events. Rollback the transaction (or abort) if the expectedRevision doesn't match
     *
     *
     * @param aggregate
     * @param expectedRevision
     * @param events
     * @return
     */
    boolean save(Aggregate aggregate, Integer expectedRevision, List<Event> events)



}