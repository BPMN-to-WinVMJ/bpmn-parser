@echo off
CALL mvn clean package
CALL copy target\bpmn-parser-1.0.0.jar ..\lib\bpmn-parser-1.0.0.jar
CALL echo JAR updated successfully!