package com.thirdchannel.eventsource.exception

/**
 * Thrown when events are loaded out of order from the eventService, typically.
 * Signifies that the implementation is not ordering by revision!
 *
 * @author Steve Pember
 */
class ChangesOutOfOrderException extends RuntimeException {}
