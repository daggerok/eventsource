package com.thirdchannel.eventsource.serialize

import com.thirdchannel.eventsource.Event

/**
 * Intended to allow users to override the standard event serialization, which uses Groovy's JsonBuilder
 *
 *
 * @author Steve Pember
 */
interface EventSerializer {

    /**
     * Given an event, must return a String representation of the Event to be persisted to disk
     *
     * @param event
     * @return
     */
    String serialize(Event event)

    /**
     * Given some serialized String Event Data, the hydrate function should convert that String into a Map containing
     * property names and values for the event
     *
     * @param data
     * @return
     */
    Map<String, Object> hydrate(String data)

}