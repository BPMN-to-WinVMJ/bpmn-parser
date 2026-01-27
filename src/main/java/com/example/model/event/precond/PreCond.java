package com.example.model.event.precond;

import com.example.model.BPMNElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class PreCond extends BPMNElement {
    public final BPMNElement xs;
    public final BPMNElement x;
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildXml'");
    }
}
