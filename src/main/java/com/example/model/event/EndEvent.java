package com.example.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class EndEvent extends Event {
    @Override
    public void buildXml(StringBuilder builder) {
        builder.append(String.format("</process>"));
    }
}
