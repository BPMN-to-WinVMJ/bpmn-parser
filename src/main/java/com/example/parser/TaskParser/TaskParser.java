package com.example.parser.TaskParser;

import java.util.HashMap;
import java.util.Map;
import com.example.model.task.*;
import com.example.model.BPMNElement;

import org.w3c.dom.Element;

public class TaskParser {

    public TaskParser() {
        registry.put("serviceTask", ServiceTask.builder());
        registry.put("userTask", UserTask.builder());
        registry.put("manualTask", ManualTask.builder());
        registry.put("receiveTask", ManualTask.builder());
        registry.put("task", ServiceTask.builder());
    }

    Map<String, Task.TaskBuilder> registry = new HashMap<>();

    public BPMNElement parse(Element el) {
        String type = el.getLocalName(); // IMPORTANT
        Task.TaskBuilder builder = registry.get(type);

        if (builder == null) {
            throw new IllegalArgumentException("Unsupported task type: " + type);
        }

        parseGeneral(el, builder);
        return builder.build();
    }

    private void parseGeneral(Element el, Task.TaskBuilder taskBuilder) {
        taskBuilder
            .id(el.getAttribute("id"))
            .name(el.getAttribute("name"));
    }
}
