package com.example.model.event.precond;

import com.example.model.BPMNElement;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartPreCond implements PreCond{
    public BPMNElement x;
}
