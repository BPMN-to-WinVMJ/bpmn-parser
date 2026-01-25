package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.flow.Flow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RepeatComponent extends Component {

    @Override
    public void buildXml(StringBuilder builder) {
        BPMNElement task = start.getOut().get(0).getTarget();
        task.buildXml(builder);
        builder.append("<while condition=\"");
        for (Flow f : end.getOut()) {
            System.err.println(f.getTarget().name);
            if (this.getElements().contains(f.getTarget())) {
                builder.append("c1 || ");
            }
        }
        builder.append("\">");
        task.buildXml(builder);
        builder.append("</while>");
    }
    
}
