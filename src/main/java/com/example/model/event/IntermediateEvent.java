package com.example.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import com.example.util.Util;

@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class IntermediateEvent extends Event {
    
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + String.format("<receive name=\"%s\"/>\n", name));
    }
    
}
