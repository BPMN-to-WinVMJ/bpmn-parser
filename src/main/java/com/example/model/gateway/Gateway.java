package com.example.model.gateway;

import com.example.model.BPMNElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public abstract class Gateway extends BPMNElement{
    Role role;

    public static enum Role {
        DIVERGING,
        MERGING,
        UNSPECIFIED
    }

    @Override
    public void buildXml(StringBuilder builder, int indent) {
        throw new UnsupportedOperationException(String.format("[GATEWAY %s] buildXml is not supposed to be called", name));
    }

    @Override
    public boolean canContinue() {
        return true;
        // throw new UnsupportedOperationException(String.format("[GATEWAY %s] canContinue is not supposed to be called", name));
    }
}
