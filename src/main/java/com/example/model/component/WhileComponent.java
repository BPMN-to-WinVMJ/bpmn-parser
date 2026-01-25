package com.example.model.component;

import com.example.model.flow.Flow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WhileComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder) {
    
        builder.append("<while>");
        for (Flow f : end.getOut()) {
            if (this.getElements().contains(f.getTarget())) {
                builder.append("<case condition=\"c1\">");
                f.getTarget().buildXml(builder);
                builder.append("</case>");
            }
        }
        builder.append("\">"); // close condition

        builder.append("</while>");
    }
    
}
