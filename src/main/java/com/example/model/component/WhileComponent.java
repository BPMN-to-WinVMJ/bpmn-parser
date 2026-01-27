package com.example.model.component;

import com.example.model.flow.Flow;
import com.example.util.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WhileComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder, int indent) {
    
        builder.append(Util.SPACE.repeat(indent) + "<while>");
        for (Flow f : end.getOut()) {
            if (this.getElements().contains(f.getTarget())) {
                builder.append(Util.SPACE.repeat(indent + 1) + "<case condition=\"c1\">");
                f.getTarget().buildXml(builder, indent + 2);
                builder.append(Util.SPACE.repeat(indent + 1) + "</case>\n");
            }
        }
        builder.append("\">"); // close condition

        builder.append(Util.SPACE.repeat(indent) + "</while>\n");
    }
    
}
