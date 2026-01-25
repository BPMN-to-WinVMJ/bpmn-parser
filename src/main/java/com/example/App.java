package com.example;

import java.io.File;

import com.example.model.BPMN;
import com.example.parser.Parser;

public class App {

    public static void main( String[] args ) {
        System.out.println("running");
        try {
            BPMN bpmn = Parser.parse(new File("demo\\src\\resource\\pick.bpmn2"));
            System.out.println(bpmn.buildXml());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
