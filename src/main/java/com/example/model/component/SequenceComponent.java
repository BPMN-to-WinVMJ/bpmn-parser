package com.example.model.component;

import com.example.model.BPMNElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SequenceComponent extends Component {
    public void buildXml(StringBuilder builder) {
        BPMNElement current = start;
        builder.append(String.format("<Sequence name=\"%s\">", name));
        while (getElements().contains(current)) {
            current.buildXml(builder);
            current = current.getOut().get(0).getTarget();
        }
        builder.append("</Sequence>");
    }
}
