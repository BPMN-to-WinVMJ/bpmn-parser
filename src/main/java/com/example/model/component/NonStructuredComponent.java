package com.example.model.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

import com.example.model.event.precond.PreCond;

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class NonStructuredComponent extends Component {
    List<List<PreCond>> preConds;

    @Override
    public void buildXml(StringBuilder builder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildXml'");
    }
}
