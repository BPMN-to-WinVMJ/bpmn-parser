package com.example.parser;

import org.w3c.dom.Element;

import com.example.model.flow.Flow;

public class FlowParser {

    public Flow parse(Element el) {
        Flow flow = new Flow();

        flow.setId(el.getAttribute("id"));
        flow.setName(el.getAttribute("name"));

        return flow;
    }
}