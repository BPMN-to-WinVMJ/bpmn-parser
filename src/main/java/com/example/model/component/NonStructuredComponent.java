package com.example.model.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

import com.example.model.BPMNElement;
import com.example.model.event.precond.EndPreCond;
import com.example.model.event.precond.FlowPreCond;
import com.example.model.event.precond.PickPreCond;
import com.example.model.event.precond.PreCond;
import com.example.model.event.precond.StartPreCond;
import com.example.model.event.precond.SwitchPreCond;
import com.example.model.flow.Flow;
import com.example.model.gateway.DataGateway;
import com.example.model.gateway.EventGateway;
import com.example.model.gateway.ParalelGateway;
import com.example.model.task.Task;
import com.example.util.Util;

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class NonStructuredComponent extends Component {
    Map<BPMNElement, List<PreCond>> preConds;

    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + "<scope name=\"").append(name).append("\">\n");
        for (BPMNElement el : preConds.keySet()) {
            for (PreCond p : preConds.get(el)) {
                String event = getPreEvent(p);
                if (el instanceof Task t) {
                    
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                        .append(Util.SPACE.repeat(indent + 2) + "<sequence>\n");
                    t.buildXml(builder, indent + 3);
                    builder.append(Util.SPACE.repeat(indent + 3) + "<invoke name=\"end(").append(el.getName()).append(")\"/>\n")
                        .append(Util.SPACE.repeat(indent + 2) + "</sequence>\n")
                        .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");
                } else if (el instanceof ParalelGateway && el.getIn().size() == 1) {
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                        .append(Util.SPACE.repeat(indent + 2) + "  <flow name=\"").append(el.getName()).append("\">\n");

                    for (Flow out : el.getOut()) {
                        builder.append(Util.SPACE.repeat(indent + 3) + "<invoke name=\"flow(")
                            .append(el.getName()).append(", ")
                            .append(out.getName()).append(")\"/>\n");
                    }

                    builder.append(Util.SPACE.repeat(indent + 2) + "</flow>\n")
                        .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");
                } else if (el instanceof DataGateway && el.getIn().size() == 1) {
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                        .append(Util.SPACE.repeat(indent + 2) + "<switch name=\"").append(el.getName()).append("\">\n");

                    for (Flow out : el.getOut()) {
                        String cond = el.getName(); // ci // TODO: Change to support cases
                        builder.append(Util.SPACE.repeat(indent + 3) + "<case condition=\"")
                            .append(cond).append("\">\n")
                            .append(Util.SPACE.repeat(indent + 4) + "<invoke name=\"switch(")
                            .append(el.getId()).append(", ")
                            .append(out.getId()).append(", ")
                            .append(cond).append(")\"/>\n")
                            .append(Util.SPACE.repeat(indent + 3) + "</case>\n");
                    }

                    builder.append(Util.SPACE.repeat(indent + 2) + "</switch>\n")
                        .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");
                } else if (el instanceof ParalelGateway && el.getIn().size() > 1) {
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                            .append(Util.SPACE.repeat(indent + 2) + "    <invoke name=\"end(").append(el.getName()).append(")\"/>\n")
                            .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");

                } else if (el instanceof DataGateway && el.getIn().size() > 1) {
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                            .append(Util.SPACE.repeat(indent + 2) + "    <invoke name=\"end(").append(el.getName()).append(")\"/>\n")
                            .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");

                } else if (el instanceof EventGateway && el.getIn().size() == 1) {
                    builder.append(Util.SPACE.repeat(indent + 1) + "<onEvent name=\"").append(event).append("\">\n")
                        .append(Util.SPACE.repeat(indent + 2) + "  <pick name=\"").append(el.getName()).append("\">\n");

                    for (Flow out : el.getOut()) {
                        builder.append(Util.SPACE.repeat(indent + 3) + "<onEvent name=\"")
                            .append(out.getId()).append("\">\n")
                            .append(Util.SPACE.repeat(indent + 4) + "<invoke name=\"pick(")
                            .append(el.getId()).append(", ")
                            .append(out.getId()).append(")\"/>\n")
                            .append(Util.SPACE.repeat(indent + 3) + "</onEvent>\n");
                    }

                    builder.append(Util.SPACE.repeat(indent + 2) + "</pick>\n")
                        .append(Util.SPACE.repeat(indent + 1) + "</onEvent>\n");
                }
            }
        }
        builder.append(Util.SPACE.repeat(indent) + "</scope>\n");
    }

    private String getPreEvent(PreCond p) {
        if (p instanceof EndPreCond) {
            return String.format("end(%s)", p.xs.name);
        } else if (p instanceof FlowPreCond) {
            return String.format("flow(%s, %s)", p.xs.name, p.x.name);
        } else if (p instanceof PickPreCond) {
            return String.format("pick(%s, %s)", p.xs.name, p.x.name);
        } else if (p instanceof StartPreCond) {
            return String.format("start()", this.name);
        } else if (p instanceof SwitchPreCond pe) {
            return String.format("switch(%s, %s %s)", pe.xs.name, pe.x.name, pe.c);
        }
        return "";
    }

    @Override
    public String getFromStartToUser(String bpmnName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFromStartToUser'");
    }
}
