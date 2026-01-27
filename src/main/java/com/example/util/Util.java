package com.example.util;

import java.util.Set;

import com.example.model.BPMN;
import com.example.model.BPMNElement;
import com.example.model.component.Component;
import com.example.model.event.IntermediateEvent;
import com.example.model.flow.Flow;
import com.example.model.task.Task;

public class Util {
    public static final String SPACE = "  ";

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
            sb.append("  ").append(isBlankOrNull(n.getName()) ? n.getId() : n.getName()).append("\n");
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

    public static void removeAllElements(BPMN bpmn, Component component) {
        bpmn.getG().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getGf().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getGm().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getGd().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getGv().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getGj().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getT().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getTr().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getE().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getEi().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
        bpmn.getEet().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
    }
    
    public static boolean isBlankOrNull(String s) {
        return s == null || "".equals(s);
    }
}
