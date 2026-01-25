package com.example.model.flow;

import com.example.model.BPMNElement;

import lombok.Data;

@Data
public class Flow {
    String id;
    String name;
    BPMNElement target;
    BPMNElement source;
}
