package com.example.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public static void setOwnerComponent(Component c) {
        for (BPMNElement e : c.getElements()) {
            e.setOwnerComponent(c);
        }
    }

    public static List<Task> traverseForward(BPMNElement e) {
        Set<BPMNElement> visited = new HashSet<>();
        List<BPMNElement> q = new ArrayList<>();
        List<Task> res = new ArrayList<>();
        q.add(e);

        while (!q.isEmpty()) {
            BPMNElement curr = q.remove(0);
            if (!visited.add(curr)) {
                continue;
            }
            if (curr instanceof Task t) {
                res.add(t);
            } else if (curr instanceof Component c) {
                res.addAll(traverseForward(c.getStart()));
            } else {
                q.addAll(curr.getOut().stream().map(x -> x.getTarget()).toList());
            }
        }
        return res;
    } 
}
