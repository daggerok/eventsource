package com.thirdchannel.eventsource.serialize

import com.thirdchannel.eventsource.Event
import com.thirdchannel.eventsource.annotation.EventData
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.lang.reflect.Field

/**
 * @author Steve Pember
 */
@Slf4j
@CompileStatic
class JsonBuilderEventSerializer implements EventSerializer {

    private JsonSlurper slurper
    private JsonBuilder builder

    JsonBuilderEventSerializer() {
        slurper = new JsonSlurper()
        builder = new JsonBuilder()
    }

    @Override
    String serialize(Event event) {
        String data = ""
        rx.Observable.from(getAllFields(event.class))
            .filter({Field f -> f.isAnnotationPresent(EventData)})
            .reduce([:], {Map agg, Field f ->
                agg[f.getName()] = event.getProperties()[f.getName()]
                agg
            })
            .map({Map it->
                builder.call(it)
                builder.toString()
            })
            .subscribe(
                {data = it},
                {log.error("Failed to Serialize data for event ${event.class.name}", it)},
                {}
        )
        data
    }

    private static List<Field> getAllFields(Class<? super Event> event) {
        List<Field> fields = []
        for (Class<? super Event> c = event; c != null; c = c.getSuperclass()) {
            fields.addAll(c.getDeclaredFields());
        }
        fields
    }

    @Override
    Map<String, Object> hydrate(String data) {
        slurper.parseText(data) as Map
    }
}
