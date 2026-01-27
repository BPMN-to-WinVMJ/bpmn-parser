package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.event.Event;
import com.example.model.flow.Flow;
import com.example.model.gateway.Gateway;
import com.example.model.task.Task;
import com.example.util.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PickComponent extends Component {

    @Override
    public void buildXml(StringBuilder builder, int indent) {
        for (Flow f : start.getOut()) {
            BPMNElement current = f.getTarget();
            while (this.getElements().contains(current)) {
                if (current instanceof Event) {
                    // TODO: change event to accept other kinds of event
                    builder.append(Util.SPACE.repeat(indent) + String.format("<receive name=%s>\n", current.getId()));
                    current = current.getOut().get(0).getTarget();
                    if (current instanceof Gateway) {
                        builder.append(Util.SPACE.repeat(indent + 1) + "<empty/>\n");
                    } else {
                        current.buildXml(builder, indent + 1);
                        current = current.getOut().get(0).getTarget();
                    }
                    builder.append(Util.SPACE.repeat(indent) + String.format("</receive>\n", current.getId()));
                    continue;
                } else if (current instanceof Task){
                    current.buildXml(builder, indent + 1);
                }
                current = current.getOut().get(0).getTarget();
            }
        }
    }
    
}
