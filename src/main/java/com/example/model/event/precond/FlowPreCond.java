package com.example.model.event.precond;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FlowPreCond extends PreCond {

    @Override
    public void buildXml(StringBuilder builder, int indent) {
    }
}
