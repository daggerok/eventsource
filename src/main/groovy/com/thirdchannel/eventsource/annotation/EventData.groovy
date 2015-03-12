package com.thirdchannel.eventsource.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Used by EventSourceService to determine which fields to Serialize within your {@link Event}s. EventData fields are
 * transient in the object, in the sense that they are not saved to the database directly, but rather, serialized into
 * JSON and written to a column (typically called 'data', unsurprisingly)
 *
 *
 * @author Steve Pember
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventData {
}
