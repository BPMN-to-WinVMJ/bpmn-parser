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
public class SwitchComponent extends Component {

    @Override
    public void buildXml(StringBuilder builder, int indent) {
        BPMNElement gf = start;
        builder.append(Util.SPACE.repeat(indent) + String.format("<switch name=\"%s\">\n", name));
        for (Flow f : gf.getOut()) {
            builder.append(Util.SPACE.repeat(indent + 1) + "<case condition=\"\">\n");
            f.getTarget().buildXml(builder, indent + 2);
            builder.append(Util.SPACE.repeat(indent + 1) + "</case>\n");
        }
        builder.append(Util.SPACE.repeat(indent) + "</Switch>\n");
    }
}
