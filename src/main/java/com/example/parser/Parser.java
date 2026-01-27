package com.example.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.example.model.*;
import com.example.model.component.Component;
import com.example.model.component.FlowComponent;
import com.example.model.component.NonStructuredComponent;
import com.example.model.component.PickComponent;
import com.example.model.component.RepeatComponent;
import com.example.model.component.SequenceComponent;
import com.example.model.component.SwitchComponent;
import com.example.model.component.WhileComponent;
import com.example.model.component.WhileRepeatComponent;
import com.example.model.event.EndEvent;
import com.example.model.event.Event;
import com.example.model.event.IntermediateEvent;
import com.example.model.event.StartEvent;
import com.example.model.event.precond.EndPreCond;
import com.example.model.event.precond.FlowPreCond;
import com.example.model.event.precond.PickPreCond;
import com.example.model.event.precond.PreCond;
import com.example.model.event.precond.StartPreCond;
import com.example.model.event.precond.SwitchPreCond;
import com.example.model.task.ReceiveTask;
import com.example.model.task.Task;
import com.example.model.flow.Flow;
import com.example.model.gateway.DataGateway;
import com.example.model.gateway.EventGateway;
import com.example.model.gateway.Gateway;
import com.example.model.gateway.ParalelGateway;
import com.example.parser.TaskParser.TaskParser;
import com.example.util.Util;

public class Parser {
    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String COMPONENT_STRING = "tc";
    private static int componentCount = 0;

    public static BPMN parse(File bpmnFile) throws Exception {
        System.out.println("Parsing..");

        BPMN bpmn = new BPMN();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(bpmnFile);

        List<Element> tasks = new ArrayList<>();

        NodeList taskNodes = doc.getElementsByTagNameNS(BPMN_NS, "task");
        NodeList serviceTaskNodes = doc.getElementsByTagNameNS(BPMN_NS, "serviceTask");
        NodeList userTaskNodes = doc.getElementsByTagNameNS(BPMN_NS, "userTask");
        NodeList manualTaskNodes = doc.getElementsByTagNameNS(BPMN_NS, "manualTask");
        NodeList receiveTaskNodes = doc.getElementsByTagNameNS(BPMN_NS, "receiveTask");

        addAll(tasks, taskNodes);
        addAll(tasks, serviceTaskNodes);
        addAll(tasks, userTaskNodes);
        addAll(tasks, manualTaskNodes);
        addAll(tasks, receiveTaskNodes);

        List<Element> events = new ArrayList<>();

        addAll(events, doc.getElementsByTagNameNS(BPMN_NS, "startEvent"));
        addAll(events, doc.getElementsByTagNameNS(BPMN_NS, "endEvent"));
        addAll(events, doc.getElementsByTagNameNS(BPMN_NS, "intermediateCatchEvent"));
        addAll(events, doc.getElementsByTagNameNS(BPMN_NS, "intermediateThrowEvent"));

        List<Element> flows = new ArrayList<>();
        addAll(flows, doc.getElementsByTagNameNS(BPMN_NS, "sequenceFlow"));

        List<Element> gateways = new ArrayList<>();
        addAll(gateways, doc.getElementsByTagNameNS(BPMN_NS, "parallelGateway"));
        addAll(gateways, doc.getElementsByTagNameNS(BPMN_NS, "exclusiveGateway"));
        addAll(gateways, doc.getElementsByTagNameNS(BPMN_NS, "eventBasedGateway"));

        TaskParser taskParser = new TaskParser();
        for (int i = 0; i < tasks.size(); i++) {
            Element element = tasks.get(i);
            Task task = (Task) taskParser.parse(element);
            bpmn.getO().put(task.getId(), task);
            bpmn.getT().put(task.getId(), task);
            if (task instanceof ReceiveTask receiveTask) {
                bpmn.getTr().put(task.getId(), receiveTask);
            }
        }

        EventParser eventParser = new EventParser();
        for (Element el : events) {
            Event event = eventParser.parse(el);

            bpmn.getO().put(event.getId(), event);
            bpmn.getE().put(event.getId(), event);

            if (event instanceof StartEvent se) {
                bpmn.getEs().put(se.getId(), se);
            }
            if (event instanceof EndEvent ee) {
                bpmn.getEe().put(ee.getId(), ee);
            }
            if (event instanceof IntermediateEvent ei) {
                bpmn.getEi().put(ei.getId(), ei);
            }
        }

        GatewayParser gatewayParser = new GatewayParser();
        for (Element el: gateways) {
            Gateway gateway = gatewayParser.parse(el);
            bpmn.getG().put(gateway.getId(), gateway);
            bpmn.getO().put(gateway.getId(), gateway);
        }

        FlowParser flowParser = new FlowParser();
        for (Element el : flows) {
            Flow flow = flowParser.parse(el);
            BPMNElement sourceElem = bpmn.getO().get(el.getAttribute("sourceRef"));
            BPMNElement targetElem = bpmn.getO().get(el.getAttribute("targetRef"));
            if ((sourceElem instanceof Task && !sourceElem.getOut().isEmpty()) ||
                targetElem instanceof Task && !targetElem.getIn().isEmpty()) {
                throw new IllegalArgumentException("Task is branching " + sourceElem.name);
            }
            flow.setSource(sourceElem);
            flow.setTarget(targetElem);
            if (!(sourceElem instanceof Gateway)) {
                sourceElem.setOut(flow);
            }
            if (!(targetElem instanceof Gateway)) {
                targetElem.setIn(flow);
            }
            if (sourceElem instanceof Gateway g) {
                g.getOut().add(flow);
            }
            if (targetElem instanceof Gateway g) {
                g.getIn().add(flow);
            }
            bpmn.getF().put(flow.getId(), flow);
        }

        for (Gateway g : bpmn.getG().values()){
            if (g instanceof ParalelGateway gf && g.getIn().size() == 1 && g.getOut().size() >= 1) {
                gf.setRole(DataGateway.Role.DIVERGING);
                bpmn.getGf().put(g.getId(), gf);
            } else if (g instanceof ParalelGateway gj && g.getIn().size() >= 1 && g.getOut().size() == 1) {
                gj.setRole(DataGateway.Role.MERGING);
                bpmn.getGj().put(g.getId(), gj);
            } else if (g instanceof DataGateway gd && g.getIn().size() == 1 && g.getOut().size() > 1) {
                gd.setRole(DataGateway.Role.DIVERGING);
                bpmn.getGd().put(g.getId(), gd);
            } else if (g instanceof DataGateway gm && g.getIn().size() > 1 && g.getOut().size() == 1) {
                gm.setRole(DataGateway.Role.MERGING);
                bpmn.getGm().put(g.getId(), gm);
            } else if (g instanceof EventGateway gv && g.getIn().size() == 1 && g.getOut().size() > 1) {
                gv.setRole(DataGateway.Role.DIVERGING);
                bpmn.getGv().put(g.getId(), gv);
            } else {
                throw new Exception(String.format("Gateway %s is both diverging and merging", g.getName()));
            }
        }

        loopFold(bpmn);
        
        return bpmn;
    }
    
    private static BPMN loopFold(BPMN bpmn) {

        List<BPMNElement> X = new ArrayList<>(bpmn.getO().values());

        // Remove start event and end event from foldable
        X.remove(bpmn.getEs().values().stream().toArray(size -> new BPMNElement[size])[0]);
        X.remove(bpmn.getEe().values().stream().toArray(size -> new BPMNElement[size])[0]);
        PriorityQueue<Component> seqComponents = findMaxSequence(bpmn);
        while (X.size() > 1) { // While foldable
            System.out.println(X.stream().map(x -> Util.isBlankOrNull(x.getName()) ? x.getId() : x.getName()).toList());
            Component component = seqComponents.poll();
            if (component != null) {
                componentCount += 1;
                component.setName(COMPONENT_STRING + componentCount);
                component.setId(COMPONENT_STRING + componentCount);
                Task enterTask = (Task) component.getElements().get(0);
                Task exitTask = (Task) component.getElements().get(component.getElements().size() -1);
                component.setIn(enterTask.getIn());
                component.setOut(exitTask.getOut());
                enterTask.getIn().get(0).setTarget(component);
                exitTask.getOut().get(0).setSource(component);
                X.removeAll(component.getElements());
                X.add(component);
                bpmn.getT().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
                bpmn.getTr().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
                bpmn.getEi().entrySet().removeIf(e -> component.getElements().contains(e.getValue()));
                System.out.println(Util.printComponent(component));
                continue;
            }
            Component nonSeq = findMaxNonSequence(bpmn);
            if (nonSeq != null) {
                componentCount += 1;
                nonSeq.setName(COMPONENT_STRING + componentCount);
                nonSeq.setId(COMPONENT_STRING + componentCount);
                for (Flow entering: nonSeq.getIn()) {
                    entering.setTarget(nonSeq);
                }
                for (Flow exiting: nonSeq.getOut()) {
                    exiting.setSource(nonSeq);
                }
                X.removeAll(nonSeq.getElements()); 
                X.add(nonSeq);
                Util.removeAllElements(bpmn, nonSeq);
                System.out.println(Util.printComponent(nonSeq));
                seqComponents = findMaxSequence(bpmn);
            } else {
                Component nonWellStructured = findMinNonWellStructuredComponent(bpmn);
                componentCount += 1;
                nonWellStructured.setName(COMPONENT_STRING + componentCount);
                nonWellStructured.setId(COMPONENT_STRING + componentCount);
                System.out.println(Util.printComponent(nonWellStructured));
                for (Flow entering: nonWellStructured.getIn()) {
                    entering.setTarget(nonWellStructured);
                }
                for (Flow exiting: nonWellStructured.getOut()) {
                    exiting.setSource(nonWellStructured);
                }
                X.removeAll(nonWellStructured.getElements()); 
                X.add(nonWellStructured);
                Util.removeAllElements(bpmn, nonWellStructured);
                seqComponents = findMaxSequence(bpmn);
            }
        }
        return bpmn;
    }

    private static PriorityQueue<Component> findMaxSequence(BPMN bpmn) {
        System.out.println("Finding max sequence..");
        PriorityQueue<Component> components = new PriorityQueue();

        List<BPMNElement> elements = new ArrayList<>();
        elements.addAll(bpmn.getT().values());
        elements.addAll(bpmn.getEi().values());
        for(BPMNElement object : elements) {
            List<BPMNElement> visited = new ArrayList<>();
            // If previous element is gateway -> is either pick
            if (bpmn.getGv().containsValue(object.getIn().get(0).getSource())) continue;
            // LEFT-MAXIMAL: do not start in the middle
            if (Util.hasOneInOut(object.getIn().get(0).getSource())) continue;

            BPMNElement current = object;
            boolean isLoop = false;
            while(true) {
                if (visited.contains(current)) {
                    isLoop = true;
                    break;
                }
                visited.add(current);
                
                // depannya diverging / converging / end event
                if (!(Util.hasOneInOut(current.getOut().get(0).getTarget())) || bpmn.getEe().containsValue(current.getOut().get(0).getTarget())) break;
                current = current.getOut().get(0).getTarget();
            }
            if (!isLoop && visited.size() >= 2) {
                components.add(SequenceComponent.builder()
                                .elements(visited)
                                .start(visited.get(0))
                                .end(visited.get(visited.size()-1))
                                .build());
            }
        }
        return components;
    }

    private static Component findMaxNonSequence(BPMN bpmn) {
        Component component = findFlow(bpmn);
        if (component != null) {
            return component;
        }
        component = findWhile(bpmn);
        if (component != null) {
            return component;
        }
        component = findRepeat(bpmn);
        if (component != null) {
            return component;
        }
        component = findRepeatWhile(bpmn);
        if (component != null) {
            return component;
        }
        component = findSwitch(bpmn);
        if (component != null) {
            return component;
        }
        component = findPick(bpmn);
        if (component != null) {
            return component;
        }
        return null;
    }

    private static Component findFlow(BPMN bpmn) {
        System.out.println("Finding flow..");
        for (ParalelGateway ic : bpmn.getGf().values()) {

            List<BPMNElement> candidates = new ArrayList<>();
            candidates.add(ic);

            List<Flow> outFlows = ic.getOut();

            if (outFlows.size() < 2) continue; // not a FLOW

            ParalelGateway oc = null;

            for (Flow f : outFlows) {
                BPMNElement mid = f.getTarget();

                // Must be Task or Intermediate Event
                if (!(Util.hasOneInOut(mid)) && !(mid instanceof StartEvent) && !(mid instanceof EndEvent)) {
                    oc = null;
                    break;
                }

                BPMNElement target = mid.getOut().get(0).getTarget();

                // All branches must converge to same join
                if (oc == null) {
                    if (!(target instanceof ParalelGateway) || ((ParalelGateway)target).getIn().size() == 1 ) break;
                    oc = (ParalelGateway) target;
                } else if (!oc.equals(target)) {
                    oc = null;
                    break;
                }

                candidates.add(mid);
            }

            if (oc == null) continue;

            // oc must be a join gateway
            if (!bpmn.getGj().containsValue(oc)) continue;

            candidates.add(oc);

            // check whether end gateway is part of something larger
            if (!oc.getIn().stream().allMatch(x -> {
                return candidates.contains(x.getSource());
            })) {
                continue;
            }
            
            Gateway oc2 = oc;
            // Verify oc incoming flows
            if (!oc.getIn().stream()
                .map(Flow::getSource)
                .collect(Collectors.toSet())
                .equals(
                    candidates.stream()
                                .filter(x -> x != ic && x != oc2)
                                .collect(Collectors.toSet())
                )) {
                continue;
            }

            return FlowComponent.builder()
                        .in(List.of(ic.getIn().get(0)))
                        .out(List.of(oc.getOut().get(0)))
                        .start(ic)
                        .end(oc)
                        .elements(candidates.stream().map(x->(BPMNElement)x).toList())
                        .build();
        }

        return null;
    }

    private static Component findSwitch(BPMN bpmn) {
        System.out.println("Finding switch..");
        for (DataGateway ic : bpmn.getGd().values()) {

            List<BPMNElement> candidates = new ArrayList<>();
            candidates.add(ic);

            List<Flow> outFlows = ic.getOut();

            if (outFlows.size() < 2) continue; // not a FLOW

            DataGateway oc = null;

            for (Flow f : outFlows) {
                BPMNElement mid = f.getTarget();

                // Must be Task or Intermediate Event
                if (!(Util.hasOneInOut(mid)) || (mid instanceof StartEvent) || (mid instanceof EndEvent)) {
                    oc = null;
                    break;
                }

                BPMNElement target = mid.getOut().get(0).getTarget();

                // All branches must converge to same join
                if (oc == null) {
                    if (!(target instanceof DataGateway) && ((DataGateway)target).getIn().size() == 1 ) break;
                    oc = (DataGateway) target;
                } else if (!oc.equals(target)) {
                    oc = null;
                    break;
                }

                candidates.add(mid);
            }

            if (oc == null) continue;

            candidates.add(oc);
            
            Gateway oc2 = oc;
            // Verify oc incoming flows
            if (!oc.getIn().stream()
                .map(Flow::getSource)
                .collect(Collectors.toSet())
                .equals(
                    candidates.stream()
                                .filter(x -> x != ic && x != oc2)
                                .collect(Collectors.toSet())
                )) {
                continue;
            }

            return SwitchComponent.builder()
                        .in(List.of(ic.getIn().get(0)))
                        .out(List.of(oc.getOut().get(0)))
                        .start(ic)
                        .end(oc)
                        .elements(candidates.stream().map(x->(BPMNElement)x).toList())
                        .build();
        }

        return null;
    }

    private static Component findPick(BPMN bpmn) {
        System.out.println("Finding pick..");
        for (EventGateway ic : bpmn.getGv().values()) {

            List<BPMNElement> candidates = new ArrayList<>();
            candidates.add(ic);

            List<Flow> outFlows = ic.getOut();

            Gateway oc = null;

            for (Flow f : outFlows) {
                BPMNElement curr = f.getTarget();

                List<BPMNElement> tempCandidates = new ArrayList<>();

                while (Util.hasOneInOut(curr) && 
                    !(curr instanceof EndEvent) && 
                    !(curr instanceof StartEvent)) {
                    tempCandidates.add(curr);
                    curr = curr.getOut().get(0).getTarget();   
                }

                // curr bukan gateway ujung
                if (!bpmn.getGm().containsValue(curr)) {
                    tempCandidates.clear();
                    oc = null;
                } else{
                    // curr itu gateway ujung
                    if (oc == null) {
                        oc = (DataGateway) curr;
                    } else {
                        if (!oc.equals(curr)) {
                            oc = null;
                            candidates.clear();
                            break;
                        }
                    }
                }

                candidates.addAll(tempCandidates);
            }

            if (oc == null) continue;

            // oc must be a join gateway
            if (!bpmn.getGm().containsValue(oc)) continue;

            candidates.add(oc);
            
            Gateway oc2 = oc;
            // Verify oc incoming flows
            if (!oc.getIn().stream()
                .map(Flow::getSource)
                .allMatch( y ->
                    candidates.stream()
                                .filter(x -> x != ic && x != oc2)
                                .collect(Collectors.toSet()).contains(y)
                )) {
                continue;
            }

            return PickComponent.builder()
                        .in(List.of(ic.getIn().get(0)))
                        .out(List.of(oc.getOut().get(0)))
                        .start(ic)
                        .end(oc)
                        .elements(candidates.stream().map(x->(BPMNElement)x).toList())
                        .build();
        }

        return null;
    }

    private static Component findWhile(BPMN bpmn) {
        System.out.println("Finding while..");

        for (DataGateway ic : bpmn.getGm().values()) {
            BPMNElement gd = ic.getOut().get(0).getTarget();

            if (bpmn.getGd().containsValue(gd)) {
                boolean isLoop = false;
                List<Flow> loopFlows = new ArrayList<>();
                List<BPMNElement> candidates = new ArrayList<>();

                candidates.add(ic);

                for (Flow f : gd.getOut()) {
                    BPMNElement mid = f.getTarget();
                    if (!Util.hasOneInOut(mid)) continue;

                    if (!(mid instanceof EndEvent) && mid.getOut().get(0).getTarget().equals(ic)){
                        isLoop = true;
                        candidates.add(mid);
                        loopFlows.add(f); // supaya flow c1 gk ke ganti targetnya
                        loopFlows.add(mid.getOut().get(0)); // supaya flow t1 -> sink gk keubah targetnya
                    }
                }
                if (isLoop) {
                    candidates.add(gd);
                    List<Flow> inFlowCopy = new ArrayList<>(ic.getIn());
                    inFlowCopy.removeAll(loopFlows);
                    List<Flow> outFlowCopy = new ArrayList<>(gd.getOut());
                    outFlowCopy.removeAll(loopFlows);

                    // component can only have 1 in and 
                    if (inFlowCopy.size() > 1 || outFlowCopy.size() > 1) continue;

                    return WhileComponent.builder()
                            .elements(candidates)
                            .in(inFlowCopy)
                            .out(outFlowCopy)
                            .start(ic)
                            .end(gd)
                            .build();
                }
            }
        }

        return null;
    }

    private static Component findRepeat(BPMN bpmn) {
        System.out.println("Finding repeat..");

        for (DataGateway ic : bpmn.getGd().values()) {

            Set<BPMNElement> candidates = new HashSet<>();
            List<Flow> loopFlows = new ArrayList<>();
            BPMNElement oc = null;

            candidates.add(ic);
            boolean isLoop = false;

            for (Flow f : ic.getOut()) {
                
                BPMNElement gm = f.getTarget();

                if (bpmn.getGm().containsValue(gm)) {

                    Flow f2 = gm.getOut().get(0);
                    BPMNElement mid = f2.getTarget();
                    
                    if (Util.hasOneInOut(mid)) {
                        if (mid.getOut().get(0).getTarget().equals(ic)) {
                            isLoop = true;
                            candidates.add(mid);
                            loopFlows.add(f);
                            loopFlows.add(f2);
                            loopFlows.add(mid.getOut().get(0));
                            oc = gm;
                        }
                    }
                }
            }
            if (isLoop){
                candidates.add(oc);
                List<Flow> outFlowCopy = new ArrayList<>(ic.getOut());
                outFlowCopy.removeAll(loopFlows);
                List<Flow> inFlowCopy = new ArrayList<>(oc.getIn());
                inFlowCopy.removeAll(loopFlows);

                // component can only have 1 in and 
                if (inFlowCopy.size() > 1 || outFlowCopy.size() > 1) continue;

                return RepeatComponent.builder()
                        .elements(List.copyOf(candidates))
                        .in(inFlowCopy)
                        .out(outFlowCopy)
                        .start(oc)
                        .end(ic)
                        .build();
            }
        }

        return null;
    }

    private static Component findRepeatWhile(BPMN bpmn) {
        System.out.println("Finding repeat while..");
        
        for (Gateway sink : bpmn.getGm().values()) {
            boolean isLoop = false;
            List<Flow> loopFlows = new ArrayList<>();
            List<BPMNElement> candidates = new ArrayList<>();
            candidates.add(sink);

            BPMNElement mid = sink.getOut().get(0).getTarget();
            if (!Util.hasOneInOut(mid)) continue;
            candidates.add(mid);

            BPMNElement gd = mid.getOut().get(0).getTarget();
            if (bpmn.getGd().containsValue(gd)) {
                Gateway diverging = (Gateway) gd;

                for (Flow f : diverging.getOut()) {
                    BPMNElement mid2 = f.getTarget();
                    if (!Util.hasOneInOut(mid2)) continue;

                    if (mid2.getOut().get(0).getTarget().equals(sink)) {
                        isLoop = true;
                        candidates.add(mid2);
                        loopFlows.add(f);
                        loopFlows.add(mid2.getOut().get(0));
                    }
                }

                if (isLoop) {
                    candidates.add(gd);
                    List<Flow> inFlowCopy = new ArrayList<>(sink.getIn());
                    inFlowCopy.removeAll(loopFlows);
                    List<Flow> outFlowCopy = new ArrayList<>(gd.getOut());
                    outFlowCopy.removeAll(loopFlows);

                    // component can only have 1 in and 
                    if (inFlowCopy.size() > 1 || outFlowCopy.size() > 1) continue;
                    
                    return WhileRepeatComponent.builder()
                            .elements(candidates)
                            .in(inFlowCopy)
                            .out(outFlowCopy)
                            .start(sink)
                            .end(gd)
                            .build();
                }
            }
        } 

        return null;
    }

    private static Component findMinNonWellStructuredComponent(BPMN bpmn) {
        System.out.println("Finding NonWellStructuredComponent");
        Component sese =  findSESE(bpmn);
        return NonStructuredComponent.builder()
                .preConds(allPreCondSets(sese))
                .elements(sese.getElements())
                .end(sese.getEnd())
                .start(sese.getStart())
                .in(sese.getIn())
                .out(sese.getOut())
                .build();
    }

    private static Component findSESE(BPMN bpmn) {
        List<Component> result = new ArrayList<>();
        
        for (Gateway ic : bpmn.getG().values()) {
            for (Gateway oc : bpmn.getG().values()) {
                
                if (ic.name.equals("g2") && oc.name.equals("g7")) {
                    System.out.println("debug");
                }

                Set<BPMNElement> region = collectNodesBetween(ic, oc);

                if (region == null) continue;
                if (!allPathsConverge(ic, oc, region)) continue;

                if (region.size() <= 2) continue;

                result.add(
                    NonStructuredComponent.builder()
                        .in(ic.getIn())
                        .out(oc.getOut())
                        .start(ic)
                        .end(oc)
                        .elements(List.copyOf(region))
                        .build()
                );   
            }
        }
        return filterMinimal(result).get(0);
    }

    private static boolean allPathsConverge(
        Gateway ic,
        Gateway oc,
        Set<BPMNElement> region) {

        HashSet<BPMNElement> visited = new HashSet<>();
        
        int count = 0;
        for (BPMNElement n : region) {
            for (Flow out : n.getOut()) {

                BPMNElement tgt = out.getTarget();

                if (oc.equals(n)) {
                    if (!region.contains(tgt)) {
                        count += 1;
                    } 
                    if (count > 1) {
                        return false;
                    }
                } else if (!pathConvergesToOC(tgt, oc, region, visited)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean pathConvergesToOC(
        BPMNElement current,
        Gateway oc,
        Set<BPMNElement> region,
        Set<BPMNElement> visited) {

        // Prevent infinite loops
        if (!visited.add(current)) {
            return true; // loop is OK, doesn't violate convergence
        }

        // If we reached oc → valid
        if (current.equals(oc)) {
            return true;
        }

        // If we exit region NOT via oc → invalid
        if (!region.contains(current)) {
            return false;
        }

        // Dead-end inside region → invalid
        if (current.getOut().isEmpty()) {
            return false;
        }

        boolean result = true;
        // All outgoing paths must converge
        for (Flow out : current.getOut()) {
            result &= pathConvergesToOC(out.getTarget(), oc, region, visited);
        }

        return result;
    }

    private static Set<BPMNElement> collectNodesBetween(
        Gateway ic,
        Gateway oc) {

        Set<BPMNElement> fromIc = new HashSet<>();
        fromIc.add(oc);
        Set<BPMNElement> toOc   = new HashSet<>();
        toOc.add(ic);

        Util.forwardDFS(ic, fromIc);
        Util.backwardDFS(oc, toOc);


        if (fromIc.stream().anyMatch(x -> toOc.contains(x)) && 
            toOc.stream().anyMatch(y -> fromIc.contains(y))) {
                return fromIc;
        }
        return null;
    }
    

    private static List<Component> filterMinimal(List<Component> candidates) {
        List<Component> minimal = new ArrayList<>();

        for (Component c : candidates) {
            boolean isMinimal = true;

            List<BPMNElement> nodesC = c.getElements();

            for (Component other : candidates) {
                if (c == other) continue;

                List<BPMNElement> nodesO = other.getElements();

                // Strict containment
                if (nodesC.containsAll(nodesO) &&
                    nodesC.size() > nodesO.size()) {

                    isMinimal = false;
                    break;
                }
            }

            if (isMinimal) {
                minimal.add(c);
            }
        }

        return minimal;
    }

    private static Map<BPMNElement, List<PreCond>> allPreCondSets(Component component){
        return component.getElements().stream()
        .collect(Collectors.toMap(
            el -> el,
            el -> preCondSet(el, component)
        ));
    }

    private static List<PreCond> preCondSet(BPMNElement el, Component c) {
        if (el instanceof DataGateway d && d.getOut().size() == 1) {
            return d.getIn().stream().map(x -> eventOnFlow(x, c)).toList();
        } else if (el instanceof ParalelGateway p) {
            return p.getIn().stream().map(x -> eventOnFlow(x, c)).toList();
        } else {
            return List.of(eventOnFlow(el.getIn().get(0), c));
        }
    }

    private static PreCond eventOnFlow(Flow f, Component c) {
        BPMNElement xs = f.getSource();
        BPMNElement x = f.getTarget();
        if (!c.getElements().contains(xs)) {
            return StartPreCond.builder()
                    .x(x)
                    .xs(null)
                    .build();
        } else if (x instanceof Task || 
            x instanceof Event || 
            (x instanceof DataGateway d && d.getOut().size() == 1) || 
            (x instanceof ParalelGateway d && d.getOut().size() == 1)
        ) {
            return EndPreCond.builder()
                    .xs(xs)
                    .build();
        } else if (x instanceof ParalelGateway) {
            return FlowPreCond.builder()
                    .xs(xs)
                    .x(x)
                    .build();
        } else if (x instanceof DataGateway) {
            return SwitchPreCond.builder()
                                .x(x)
                                .xs(xs)
                                .c(f.getName())
                                .build();
        } else if (x instanceof EventGateway) {
            return PickPreCond.builder().x(x).xs(xs).build();
        }
        return null;
    }

    private static void addAll(List<Element> list, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add((Element) nodes.item(i));
        }
    }

}
