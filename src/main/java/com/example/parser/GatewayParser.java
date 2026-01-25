package com.example.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.w3c.dom.Element;

import com.example.model.gateway.DataGateway;
import com.example.model.gateway.EventGateway;
import com.example.model.gateway.Gateway;
import com.example.model.gateway.ParalelGateway;

public class GatewayParser {

    public GatewayParser() {
        registry.put("parallelGateway", ParalelGateway::builder);
        registry.put("exclusiveGateway", DataGateway::builder);
        registry.put("eventBasedGateway", EventGateway::builder);
    }

    Map<String, Supplier<? extends Gateway.GatewayBuilder<?, ?>>> registry = new HashMap<>();

    public Gateway parse(Element el) {
        String type = el.getLocalName();
        Supplier<? extends Gateway.GatewayBuilder<?, ?>> supplier = registry.get(type);
        if (supplier == null) {
            throw new IllegalArgumentException("Unsupported gateway type: " + type);
        }

        return supplier.get()
                .id(el.getAttribute("id"))
                .name(el.getAttribute("name"))
                .build();
    }
}
