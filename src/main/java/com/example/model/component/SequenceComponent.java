package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.util.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SequenceComponent extends Component {

    @Override
    public void buildXml(StringBuilder builder, int indent) {
        BPMNElement current = start;
        builder.append(Util.SPACE.repeat(indent) + String.format("<sequence name=\"%s\">\n", name));
        while (getElements().contains(current)) {
            current.buildXml(builder, indent + 1);
            current = current.getOut().get(0).getTarget();
        }
        builder.append(Util.SPACE.repeat(indent) + "</sequence>\n");
    }
}
