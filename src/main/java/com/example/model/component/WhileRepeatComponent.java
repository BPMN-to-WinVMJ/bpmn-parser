package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.flow.Flow;
import com.example.util.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WhileRepeatComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        BPMNElement task = start.getOut().get(0).getTarget();
        task.buildXml(builder, indent);
        for (Flow f : end.getOut()) {
            if (this.getElements().contains(f.getTarget())) {
                builder.append(Util.SPACE.repeat(indent) + "<while condition=\"c1\">\n");
                f.getTarget().buildXml(builder, indent + 1);
                builder.append(Util.SPACE.repeat(indent) + "</while>\n");
            }
        }
    }

    @Override
    public String getFromStartToUser(String bpmnName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFromStartToUser'");
    }
    
}
