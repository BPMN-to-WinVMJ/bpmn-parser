package com.example.model.component;

import com.example.model.BPMNElement;
import com.example.model.flow.Flow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SwitchComponent extends Component {
    public void buildXml(StringBuilder builder) {
        BPMNElement gf = start;
        builder.append(String.format("<Switch name=\"%s\" >", name));
        for (Flow f : gf.getOut()) {
            builder.append("<case condition=\"\"");
            f.getTarget().buildXml(builder);
            builder.append("</case>");
        }
        builder.append("</Switch>");
    }
}
