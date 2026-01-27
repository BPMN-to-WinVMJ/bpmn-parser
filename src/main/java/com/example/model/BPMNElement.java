package com.example.model;

import java.util.ArrayList;
import java.util.List;

import com.example.model.flow.Flow;

import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BPMNElement {
    @EqualsAndHashCode.Include
    public String id;
    public String name;

    @Default
    List<Flow> in = new ArrayList<>();
    @Default
    List<Flow> out = new ArrayList<>();

    public void setIn(List<Flow> f){
        in = f;
    }
    public void setOut(List<Flow> f){
        out = f;
    }

    public void setIn(Flow f){
        in = List.of(f);
    }
    public void setOut(Flow f){
        out = List.of(f);
    }

    public abstract void buildXml(StringBuilder builder, int indent);
}