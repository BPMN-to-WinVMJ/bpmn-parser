package com.example.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.model.event.EndEvent;
import com.example.model.event.Event;
import com.example.model.event.IntermediateEvent;
import com.example.model.event.StartEvent;
import com.example.model.flow.Flow;
import com.example.model.gateway.DataGateway;
import com.example.model.gateway.EventGateway;
import com.example.model.gateway.Gateway;
import com.example.model.gateway.ParalelGateway;
import com.example.model.task.ReceiveTask;
import com.example.model.task.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BPMN {
    @Default
    Map<String, BPMNElement> O = new HashMap<>(); // Task, Gateway, Event

    @Default
    Map<String, Task> T = new HashMap<>();

    @Default
    Map<String, ReceiveTask> Tr = new HashMap<>();

    @Default
    Map<String, Event> E = new HashMap<>();

    @Default
    Map<String, StartEvent> Es = new HashMap<>();

    @Default
    Map<String, IntermediateEvent> Ei = new HashMap<>();

    @Default
    Map<String, EndEvent> Ee = new HashMap<>();

    @Default
    Map<String, IntermediateEvent> Eet = new HashMap<>();

    @Default
    Map<String, Gateway> G = new HashMap<>();

    @Default
    Map<String, ParalelGateway> Gf = new HashMap<>(); // paralel fork

    @Default
    Map<String, ParalelGateway> Gj = new HashMap<>(); // paralel fork join

    @Default
    Map<String, DataGateway> Gd = new HashMap<>(); // data xor

    @Default
    Map<String, EventGateway> Gv = new HashMap<>(); // event xor

    @Default
    Map<String, DataGateway> Gm = new HashMap<>(); // merge xor

    @Default
    Map<String, Flow> F =new HashMap<>();

    public String buildXml() {
        StringBuilder builder = new StringBuilder();
        BPMNElement current = List.copyOf(Es.values()).get(0);
        current.buildXml(builder);
        current = current.getOut().get(0).getTarget();
        current.buildXml(builder);
        current = current.getOut().get(0).getTarget();
        current.buildXml(builder);
        return builder.toString();
    }
}


