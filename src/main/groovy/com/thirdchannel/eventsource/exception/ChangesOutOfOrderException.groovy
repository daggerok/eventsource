package com.thirdchannel.eventsource.exception

/**
 * @author Steve Pember
 *
 * thrown when events are loaded out of order from the eventService, typically.
 * Signifies that the implementation is not ordering by revision!
 */
class ChangesOutOfOrderException extends RuntimeException {
}
