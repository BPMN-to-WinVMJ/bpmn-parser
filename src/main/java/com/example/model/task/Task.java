package com.example.model.task;

import com.example.model.BPMNElement;
import com.example.model.component.Component;
import com.example.model.component.NonStructuredComponent;
import com.example.model.flow.Flow;
import com.example.model.gateway.Gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import com.example.util.Util;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public abstract class Task extends BPMNElement {
    @EqualsAndHashCode.Exclude
    public boolean fromStart;

    //TODO: Change this to enable all types of task
    @Override
    public void buildXml(StringBuilder builder, int indent) {
        builder.append(Util.SPACE.repeat(indent) + String.format("<invoke name=\"%s\">\n", name));
    }

    //TODO: Change this to enable all types of task
    @Override
    public String buildService(int indent, boolean impl) {
        StringBuilder builder = new StringBuilder();
        builder.append(Util.SPACE.repeat(indent) + String.format("public void %s(Map<String, Object> body, String processid)", name.replace(" ", "")));
        if (impl) {
            builder.append(" {\n");
            builder.append(Util.SPACE.repeat(indent + 2) + "//TODO implement this\n");
            builder.append(Util.SPACE.repeat(indent + 2) + String.format("processService.upsert(new ProcessInstance(processid, \"%s\"));\n", name.replace(" ", "")));
            builder.append(Util.SPACE.repeat(indent) + "}\n\n");
        } else {
            builder.append(";\n");
        }
        return builder.toString();
    }

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
        return "";
    }

    protected String getServiceTaskAfter(String bpmnName) {
        StringBuilder builder = new StringBuilder();
        BPMNElement curr = getOut().get(0).getTarget();
        while (curr.canContinue()) {
            if (curr instanceof Component c) {
                builder.append(
                    c.getFromStartToUser(bpmnName)
                );
            } else {
                if (curr instanceof Task) {
                    builder.append(
                        String.format(
                            "%sService.%s(requestBody, processid);\n", 
                            bpmnName, curr.getName()
                        )
                    );
                } else if (curr instanceof Gateway && curr.getOut().size() > 1) {
                    boolean first = true;

                    for (Flow f : curr.getOut()) {
                        BPMNElement branchStart = f.getTarget();

                        String res = emitLinearServiceBranch(builder, branchStart, bpmnName);

                        if (first && !res.isEmpty()) {
                            builder.append(
                                String.format("if (%s) {\n", f.getName())
                            );
                            first = false;
                        } else if (!res.isEmpty()) {
                            builder.append(
                                String.format("} else if (%s) {\n", f.getName())
                            );
                        }

                        emitLinearServiceBranch(builder, branchStart, bpmnName);
                    }
                    if (!first){
                        builder.append("}\n");
                    }
                        
                    return builder.toString();
                    
                }
            }
            curr = curr.getOut().get(0).getTarget();
        }
        if (curr instanceof Component c) {builder.append(c.getFromStartToUser(bpmnName));}
        return builder.toString();
    }

    private String emitLinearServiceBranch(
        StringBuilder builder,
        BPMNElement start,
        String bpmnName
    ) {
        BPMNElement curr = start;

        while (curr != null && curr.canContinue()) {

            // Nested user interaction → delegate
            if (curr instanceof Component c) {
                builder.append(
                    c.getFromStartToUser(bpmnName)
                        .indent(1)   // optional, if you manage indentation
                );
                return builder.toString();
            } else if (curr instanceof Task) {
                builder.append(
                    String.format(
                        "\t%sService.%s(requestBody, processid);\n",
                        bpmnName,
                        curr.getName()
                    )
                );
            }

            // Nested gateway → emit nested branching
            if (curr instanceof Gateway g && curr.getOut().size() > 1) {
                boolean first = true;

                for (Flow f : g.getOut()) {
                    if (first) {
                        builder.append(
                            String.format("\tif (%s) {\n", f.getName())
                        );
                        first = false;
                    } else {
                        builder.append(
                            String.format("\t} else if (%s) {\n", f.getName())
                        );
                    }

                    emitLinearServiceBranch(
                        builder,
                        f.getTarget(),
                        bpmnName
                    );
                }

                builder.append("\t}\n");
                return builder.toString();
            }

            if (curr.getOut().isEmpty()) {
                return builder.toString();
            }

            curr = curr.getOut().get(0).getTarget();
        }
        return builder.toString();
    }

    @Override
    public boolean canContinue() {
        return true;
    }
}
