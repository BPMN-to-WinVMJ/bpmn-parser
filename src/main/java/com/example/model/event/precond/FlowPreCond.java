package com.example.model.event.precond;

import com.example.model.BPMNElement;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FlowPreCond implements PreCond {
    public BPMNElement x;
    public BPMNElement xs;
}
