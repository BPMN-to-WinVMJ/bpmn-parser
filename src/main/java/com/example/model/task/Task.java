package com.example.model.task;

import com.example.model.BPMNElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public abstract class Task extends BPMNElement {

    //TODO: Change this to enable all types of task
    @Override
    public void buildXml(StringBuilder builder) {
        builder.append(String.format("<invoke name=\"%s\">", name));
    }
}
