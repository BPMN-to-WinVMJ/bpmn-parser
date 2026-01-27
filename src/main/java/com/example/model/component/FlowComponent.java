package com.example.model.component;

import com.example.model.flow.Flow;

import com.example.util.Util;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class FlowComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + String.format("<flow name=\"%s\">\n", name));
        for (Flow f : start.getOut()) {
            f.getTarget().buildXml(builder, indent + 1);
        }
        builder.append(Util.SPACE.repeat(indent) + "</flow>\n");
    }
}
