package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.flow.Flow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WhileRepeatComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder) {
        BPMNElement task = start.getOut().getFirst().getTarget();
        task.buildXml(builder);
        for (Flow f : end.getOut()) {
            System.err.println(f.getTarget().name);
            if (this.getElements().contains(f.getTarget())) {
                builder.append("<while condition=\"c1\">");
                f.getTarget().buildXml(builder);
                builder.append("</while>");
            }
        }
    }
    
}
