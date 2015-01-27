package com.thirdchannel.eventsource

/**
 * @author steve pember
 *
 *
 * An event reflects a change that occurred within your system, as well as the id of the user which performed the event.
 * Events reflect the *intent* of user action, based on the successful completion of a user action or command.
 *
 * An event should save its 'transient' data as a json string in the database, and restore it on load. This helps keep
 * the database columns for event immutable yet allows for changing of events by simply adding new fields into the underlying json map.
 * Furthermore, all events can go in the same table.
 *
 * I realize that this argument can seem ridiculous, e.g. 'Why don't we just serialize ALL or records to json and make
 * one big table!'
 *
 * However, if
 *
 *
 */
public interface Event {

    UUID getId()

    Integer getRevision()

    void setRevision(Integer revision)

    UUID getAggregateId()

    Date getDate()

    /**
     * Returns the Class name of the event, to be used for mapping data into a POJO
     *
     * @return String the class name
     */
    String getClazz()

    /**
     *
     * @return String the aforementioned serialized data unique to this event. Recommended to be JSON
     */
    String getData()

    /**
     * The user id of the user responsible for the event's generation. Return type is String in order to help generalize
     * what can be used as the user id (e.g. String, some int, perhaps a UUID.
     *
     * @return String the user Id.
     */
    String getUserId()

    // todo: consider a process Id or command Id field? This would help group events by the action which generated them

    /**
     * Copy individual fields out of the data object and into the sub class
     *
     *
     * @param data the converted
     */
    void restoreData(Map data)

    /**
     * Apply this event to the aggregate. Should somehow apply data contained within this event into the aggregate
     *
     * @param root
     */
    void process(Aggregate root)



}