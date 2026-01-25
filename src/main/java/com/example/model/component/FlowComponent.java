package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.flow.Flow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class FlowComponent extends Component {
    
    public void buildXml(StringBuilder builder) {
        builder.append(String.format("<Flow name=\"%s\" >", name));
        for (Flow f : start.getOut()) {
            f.getTarget().buildXml(builder);
        }
        builder.append("</Flow>");
    }
}
