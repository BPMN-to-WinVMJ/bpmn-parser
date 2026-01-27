package com.example.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import com.example.util.Util;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class EndEvent extends Event {
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + String.format("</process>\n"));
    }
}
