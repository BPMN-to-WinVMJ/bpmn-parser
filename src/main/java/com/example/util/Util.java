package com.example.util;

import java.util.Set;

import com.example.model.BPMNElement;
import com.example.model.component.Component;
import com.example.model.event.IntermediateEvent;
import com.example.model.flow.Flow;
import com.example.model.task.Task;

public class Util {
    public static boolean hasOneInOut(BPMNElement e){
        return e instanceof Task || 
                e instanceof IntermediateEvent || 
                (
                    e instanceof Component && 
                    e.getIn().size() == 1 &&
                    e.getOut().size() == 1
                );
    }

    public static String printComponent(Component c) {
        StringBuilder sb = new StringBuilder();

        sb.append("Component ").append(c.getName()).append("\n");
        sb.append("  Entry: ").append(c.getIn().stream().map(x->x.getSource()).filter(x -> !x.equals(c)).map(x -> x.getName()).toList()).append("\n");

        for (BPMNElement n : c.getElements()) {
            sb.append("  ").append(n.getId()).append("\n");
        }

        sb.append("  Exit : ").append(c.getOut().stream().map(x->x.getTarget()).filter(x -> !x.equals(c)).map(x -> x.getName()).toList()).append("\n\n");

        return sb.toString();
    }

    public static void forwardDFS(
        BPMNElement current,
        Set<BPMNElement> visited) {

        if (!visited.add(current)) return;

        for (Flow out : current.getOut()) {
            forwardDFS(out.getTarget(), visited);
        }
    }

    public static void backwardDFS(
        BPMNElement current,
        Set<BPMNElement> visited) {

        if (!visited.add(current)) return;

        for (Flow in : current.getIn()) {
            backwardDFS(in.getSource(), visited);
        }
    }
    
}
