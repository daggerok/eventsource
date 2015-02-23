package com.thirdchannel.eventsource

import groovy.transform.CompileStatic

/**
 * Reflects a change that occurred within your system, as well as the id of the user which performed the event.
 * Reflects the *intent* of user action, based on the successful completion of a user action or command.
 *
 * An event should save its 'transient' data as a JSON string in the database, and restore it on load. This helps keep
 * the database columns for event immutable yet allows for changing of events by simply adding new fields into the underlying JSON map.
 * Furthermore, all events can go in the same table.
 *
 * I realize that this argument can seem ridiculous, e.g. 'Why don't we just serialize ALL or records to json and make
 * one big table!'
 *
 * However, if
 *
 * @author steve pember
 */
@CompileStatic
interface Event {

    UUID getId()
    void setId(UUID id)

    int getRevision()
    void setRevision(int revision)

    UUID getAggregateId()
    void setAggregateId(UUID id)

    Date getDate()
    void setDate(Date date)

    /**
     * The Class name of the event, for mapping data into a POJO
     *
     * @return the name
     */
    String getClazz()

    void setClazz(String clazz)

    /**
     * @return the aforementioned serialized data unique to this event. Recommended to be JSON
     */
    String getData()
    void setData(String s)

    /**
     * The id of the user responsible for the event's generation. Return type is String to help generalize
     * what can be used as the user id (e.g. String, some int, perhaps a UUID).
     *
     * @return the id.
     */
    String getUserId()

    void setUserId(String userId)

    // todo: consider a process Id or command Id field? This would help group events by the action which generated them

    /**
     * Copy individual fields from the data object to the sub class
     *
     * @param data the converted
     */
    void restoreData(Map data)

    /**
     * Apply this event to the aggregate. Should apply event data into the aggregate
     */
    void process(Aggregate root)
}
