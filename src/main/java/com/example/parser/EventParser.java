package com.example.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.w3c.dom.Element;

import com.example.model.event.EndEvent;
import com.example.model.event.Event;
import com.example.model.event.IntermediateEvent;
import com.example.model.event.StartEvent;

public class EventParser {

    private final Map<String, Event.EventBuilder> registry = new HashMap<>();

    public EventParser() {
        registry.put("startEvent", StartEvent.builder());
        registry.put("endEvent", EndEvent.builder());
        registry.put("intermediateCatchEvent", IntermediateEvent.builder());
        registry.put("intermediateThrowEvent", IntermediateEvent.builder());
    }

    public Event parse(Element el) {
        String type = el.getLocalName(); // IMPORTANT
        Event.EventBuilder builder = registry.get(type);

        if (builder == null) {
            throw new IllegalArgumentException("Unsupported event type: " + type);
        }

        Event event = builder.build();
        parseGeneral(el, event);
        return event;
    }

    private void parseGeneral(Element el, Event event) {
        event.setId(el.getAttribute("id"));
        event.setName(el.getAttribute("name"));
    }
}
