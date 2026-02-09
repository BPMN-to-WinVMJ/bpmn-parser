package com.example.model.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.model.BPMNElement;
import com.example.model.component.Component;
import com.example.model.component.FlowComponent;
import com.example.model.component.RepeatComponent;
import com.example.model.component.WhileRepeatComponent;
import com.example.model.event.Event;
import com.example.model.gateway.Gateway;
import com.example.model.gateway.ParalelGateway;
import com.example.util.Util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class UserTask extends Task {

    // So far I believe this code should only be called when there is only 1 task in BPMN Diagram 
    @Override
    public String buildResource(String bpmnName, int indent) {
        StringBuilder builder = new StringBuilder();
        String taskName = name.replace(" ", "");
        if (fromStart) {
            builder.append(
                String.format(
                    """
                    @Route(url = "call/%s")
                    public Map<String, Object> %s(VMJExchange e) {
                    
                        Map<String, Object> res = new HashMap<>();
                        String processid = UUID.randomUUID().toString();
                        res.put("processInstanceId", processid);
                        res.put("message", "%s Successful");
                        processService.upsert(new ProcessInstance(processid, "%s"));

                        %sService.%s(requestBody, processid);

                        %s

                        return res;
                    }
                    """,
                    taskName,
                    taskName,
                    taskName,
                    taskName,
                    bpmnName,
                    taskName,
                    getServiceTaskAfter(bpmnName)
                )
            );
            return builder.toString();
        }
        builder.append(
            String.format(
                """
                @Route(url = "call/%s")
                public Map<String,Object> %s(VMJExchange vmjExchange){
                    Map<String, Object> res = new HashMap<>();
                    Map<String, Object> requestBody = vmjExchange.getPayload();
                    String processid = (String) requestBody.get("processInstanceId");

                    if (vmjExchange.getHttpMethod().equals("POST")) {

                        // Cek apakah step sebelumnya pernah dilakukan
                        // This also allows user yang mundur page trus isi form ulang
                        // karena langkah sebelum page ini pasti udh dilakukan
                        // ini juga mencegah orang dari asal tembak api
                        List<ProcessInstance> processes = processService.getAllById(processid);
                        if (%s
                        ) {
                            res.put("message", "%s DENIED");
                            return res;
                        }

                        processService.upsert(new ProcessInstance(processid, "%s"));
                        
                        %sService.%s(requestBody, processid);
                        %s
                        res.put("message", "%s SUCCESS");

                        return res;
                    }
                    return res;
                }
                """,
                taskName,
                taskName,
                getPriors(indent, isStartOfDoWhile() ? Util.traverseForward(this.getOut().get(0).getTarget()) : List.of()),
                taskName,
                taskName,
                bpmnName,
                taskName,
                getServiceTaskAfter(bpmnName),
                taskName
            )
        );
        return builder.toString();
    }

    private String getPriors(int indent, List<Task> after) {
        List<BPMNElement> result = new ArrayList<>();
        BPMNElement prev = getIn().get(0).getSource();
        String join = "||";
        if (prev instanceof Component c) {
            prev = c.getEnd();
            while (prev instanceof Component c2) {
                prev = c2.getEnd();
            }
            if (prev instanceof Task t) {
                result.add(t);
            } else {
                result.addAll(traverseBackwards(prev));
            }
        } else {
            if (prev instanceof Task t) {
                result.add(t);
            } else if (prev instanceof Gateway g) {
                result.addAll(traverseBackwards(g));
                if (g instanceof ParalelGateway && g.getIn().size() > 1) {
                    join = "&&";
                }
                if (g instanceof ParalelGateway && g.getOut().size() > 1 &&
                    (g.getIn().get(0).getSource() instanceof FlowComponent || 
                    g instanceof ParalelGateway && g.getIn().size() > 1)
                ) {
                    join = "&&";
                }
            } else if (prev instanceof Event e) {
                result.add(e);
            }
        }

        // Remove all elements in front of this task from backwards travers, 
        // usually comes from loop
        result.removeAll(after);

        String joinFinal = join;
        
        StringBuilder builder = new StringBuilder();
        builder.append("x -> ");
        for (BPMNElement e : result) {
            builder.append(
                String.format(
                    "x.state.equalsIgnoreCase(\"%s\") %s\r\n", 
                    e.getName().replaceAll(" ", ""), 
                    joinFinal
                )
            );
        }
        // no additional filter
        if (result.isEmpty()) {
            builder.replace(0, 5, "true");
        } else {
            builder.replace(builder.length() - 4, builder.length(), "");
        }
        StringBuilder builder2 = new StringBuilder();
        builder2.append("processes.stream()\n");
        builder2.append(Util.SPACE.repeat(indent + 2) + ".filter(\n");
        builder2.append(Util.SPACE.repeat(indent + 4) + builder.toString() + "\n");
        builder2.append(Util.SPACE.repeat(indent + 2) + ").toList().isEmpty()");
        return builder2.toString();
    }

    private List<Task> traverseBackwards(BPMNElement e) {
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
                res.addAll(traverseBackwards(c.getEnd()));
            } else {
                q.addAll(curr.getIn().stream().map(x -> x.getSource()).toList());
            }
        }
        return res;
    }

    private boolean isStartOfDoWhile() {
        Component parent = this.getOwnerComponent();
        while (
            parent != null
            && !(parent instanceof WhileRepeatComponent)
            && !(parent instanceof RepeatComponent)
        ) {
            parent = parent.getOwnerComponent();
        }
        if (parent == null) return false;
        return parent.getStart().getOut().get(0).getTarget().equals(this);
    }

    @Override
    public boolean canContinue() {
        return false;
    }
}
