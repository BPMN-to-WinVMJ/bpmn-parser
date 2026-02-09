package com.example.model.event;

import com.example.model.BPMNElement;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public abstract class Event extends BPMNElement {
    @Override
    public boolean canContinue() {
        return true;
    }
}
