package com.example.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class StartEvent extends Event {
    @Override
    public void buildXml(StringBuilder builder) {
        builder.append(String.format("<process>"));
    }
}
