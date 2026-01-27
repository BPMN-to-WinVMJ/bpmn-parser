package com.example.model.event.precond;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SwitchPreCond extends PreCond {
    public String c;

    @Override
    public void buildXml(StringBuilder builder, int indent) {
    }
}
