package com.example.model.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;


import java.util.List;

import com.example.model.BPMNElement;

import java.util.ArrayList;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class Component extends BPMNElement implements Comparable<Component> {
    @Default
    public List<BPMNElement> elements = new ArrayList<>();
    
    public BPMNElement start;
    public BPMNElement end;
    
    @Override
    public int compareTo(Component other) {
        return Integer.compare(
            this.elements.size(),
            other.elements.size()
        );
    }
}

