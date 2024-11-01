package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Main {


    @Data
    @AllArgsConstructor
    public static class Student {
        String name;
        int age;
        String address;
        String level;
    }

    public static void main(String[] args) {
        Student student1 = new Student("suraj", 18, "North-york", "Java7");
        Student student2 = new Student("rocker", 22, "West-york", "Java8");
        Student student3 = new Student("suad", 20, "Yeast-york", "Java17");


        List<Student> studentList = Arrays.asList(student1, student2, student3);
    studentList.stream()
                .filter(student -> student.age > 19)
                .map(student -> new Student(student.getName().toUpperCase(), student.getAge(), student.getAddress().toUpperCase(), student.getLevel().toUpperCase()))
                .forEach(s-> Main.printStudent(s));
//
//        for (Student student : collect) {
//            Main.printStudent(student);
//        }

        useOfFlatMapJavaEight();
    }


    //this is consumer
    private static void printStudent(Student student) {
        log.info("student Name " + student.name);
        log.info("student age " + student.age);
        log.info("student Address " + student.address);
        log.info("student level " + student.level);
    }

    private static void useOfFlatMapJavaEight(){
        List<List<String>> cityList = Arrays.asList(
                Arrays.asList(
                        "Toronto", "NorthYork", "Mississauga", "Brampton"
                )
        );

        List<String> collect = cityList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

//        cityList.stream()
//                        .filter(city -> city.stream().filter(c -> c.startsWith("T"))
//                                .collect(Collectors.toList());


        System.out.println("collect: " + collect);

        System.out.println("Nested without Flat-map " + cityList);

    }


}