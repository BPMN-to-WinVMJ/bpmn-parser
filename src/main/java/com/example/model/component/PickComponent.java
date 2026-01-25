package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.event.Event;
import com.example.model.flow.Flow;
import com.example.model.gateway.Gateway;
import com.example.model.task.Task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PickComponent extends Component {

    @Override
    public void buildXml(StringBuilder builder) {
        for (Flow f : start.getOut()) {
            BPMNElement current = f.getTarget();
            while (this.getElements().contains(current)) {
                if (current instanceof Event) {
                    // TODO: change event to accept other kinds of event
                    builder.append(String.format("<receive name=%s>", current.getId()));
                    current = current.getOut().get(0).getTarget();
                    if (current instanceof Gateway) {
                        builder.append("<empty/>");
                    } else {
                        current.buildXml(builder);
                        current = current.getOut().get(0).getTarget();
                    }
                    builder.append(String.format("</receive>", current.getId()));
                    continue;
                } else if (current instanceof Task){
                    current.buildXml(builder);
                }
                current = current.getOut().get(0).getTarget();
            }
        }
    }
    
}
