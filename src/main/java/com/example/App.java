package com.example;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.model.BPMN;
import com.example.model.event.StartEvent;
import com.example.model.task.Task;
import com.example.model.task.UserTask;
import com.example.parser.Parser;
import com.example.util.Util;
import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;

public class App {

    public static void main( String[] args ) {
        System.out.println("running");
        try {
            String bpmnName = "deepbranch";
            File file = new File(String.format("src\\resource\\%s.bpmn2", bpmnName));
            BPMN bpmn = Parser.parse(file);
            List<Task> tasks = List.copyOf(bpmn.getT().values().stream().toList());
            StartEvent start = bpmn.getEs().values().stream().toList().get(0); // takes start event
            List<Task> startingTask = Util.traverseForward(start.getOut().get(0).getTarget());
            Set<Task> userTasks = new HashSet<>(bpmn.getT().values().stream().filter(x -> x instanceof UserTask).toList());
            System.out.println("zczc "+ userTasks.stream().map(x->x.getName()).toList());
            for (Task starting : startingTask) {
                starting.setFromStart(true);
                userTasks.add(starting);
            }
            System.out.println("zczc "+ userTasks.stream().map(x->x.getName()).toList());
            
            Parser.loopFold(bpmn);
            
            System.out.println(bpmn.buildXml());
            Jinjava jinjava = new Jinjava();

            Map<String, Object> contextResource = Maps.newHashMap();
            contextResource.put("bpmnName", bpmnName);

            System.out.println("zczc "+ tasks.stream().map(x->x.getName()).toList());
            System.out.println("zczc "+ userTasks.stream().map(x->x.getName()).toList());
            contextResource.put("tasks", tasks);
            contextResource.put("userTasks", userTasks);
            contextResource.put("builder", new StringBuilder());

            String templateResource = Files.readString(
                Path.of("src/main/java/com/example/template/resource.jinja"),
                StandardCharsets.UTF_8
            );
            // String templateService = Files.readString(
            //     Path.of("src\\main\\java\\com\\example\\template\\service.jinja"),
            //     StandardCharsets.UTF_8
            // );

            String renderedResourceTemplate = jinjava.render(templateResource, contextResource);
            // String renderedServiceTemplate = jinjava.render(templateService, contextRService);

            System.out.println(renderedResourceTemplate);
            // System.out.println(renderedServiceTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
