package com.example.model.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.model.flow.Flow;
import com.example.model.BPMNElement;

import com.example.util.Util;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class FlowComponent extends Component {
    
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + String.format("<flow name=\"%s\">\n", name));
        for (Flow f : start.getOut()) {
            f.getTarget().buildXml(builder, indent + 1);
        }
        builder.append(Util.SPACE.repeat(indent) + "</flow>\n");
    }

    @Override
    public String getFromStartToUser(String bpmnName) {
        StringBuilder builder = new StringBuilder();
        Set<BPMNElement> visited = new HashSet<>();

        List<Flow> outs = getStart().getOut();
        boolean first = true;

        for (Flow f : outs) {
            if (first) {
                builder.append(String.format("if (%s) {\n", f.getName()));
                first = false;
            } else {
                builder.append(String.format("} else if (%s) {\n", f.getName()));
            }

            buildResource(builder, bpmnName, f.getTarget(), visited, 1);
        }

        builder.append("}\n");
        return builder.toString();
    }

    public void buildResource(
        StringBuilder builder,
        String bpmnName,
        BPMNElement el,
        Set<BPMNElement> visited,
        int indent
    ) {
        BPMNElement curr = el;

        while (curr != null && visited.add(curr)) {

            // emit task/component
            builder.append(Util.SPACE.repeat(indent))
                .append(curr.getName())
                .append("()\n");

            // first blocking element ends the branch
            if (!curr.canContinue()) {
                builder.append("\treturn res\n");
                return;
            } else if (curr instanceof Component c) {
                builder.append(c.getFromStartToUser(bpmnName));
            }

            // already flattened → either 0 or 1
            if (curr.getOut().isEmpty()) {
                return;
            }

            curr = curr.getOut().get(0).getTarget();
        }
    }

    //TODO: [P0] FIX CANContinue to not return true if any has usertask
    // MAYBE WE CAN ASSUME THAT USER IF SLOW ENOUGH SO THAT IF THERE IS A USER TASK
    // IN ONE BRANCH, WE CAN LEAVE IT BE
    // PROBLEM OCCUR IF THERE ARE >2 BRANCH THAT HAVE USERTASK
    @Override
    public boolean canContinue() {
        Set<BPMNElement> visited = new HashSet<>();

        for (Flow f : getStart().getOut()) {
            if (canContinueFrom(f.getTarget(), visited)) {
                return true; // at least one branch can continue
            }
        }

        // all branches are blocked
        return false;
    }

    private boolean canContinueFrom(BPMNElement curr, Set<BPMNElement> visited) {
        // prevent infinite loops
        if (!visited.add(curr)) {
            return false;
        }

        // this element itself blocks continuation
        if (!curr.canContinue()) {
            return false;
        }

        // no outgoing flow = end node → still valid continuation
        if (curr.getOut().isEmpty()) {
            return true;
        }

        // OR semantics: if ANY outgoing branch can continue, we're good
        for (Flow f : curr.getOut()) {
            if (canContinueFrom(f.getTarget(), visited)) {
                return true;
            }
        }

        // all downstream branches are blocked
        return false;
    }
}
