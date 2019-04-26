package com.finalproject.automated.refactoring.tool;

import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
        Long startTime = System.nanoTime();

        applicationContext.getBean(Demo.class)
                .doCodeSmellsDetection(Arrays.asList(args));

        Long endTime = System.nanoTime();
        printRunTime(startTime, endTime);
        applicationContext.close();
    }

    private static void printRunTime(Long startTime, Long endTime) {
        double runTime = (double) (endTime - startTime);
        runTime /= 1000000000;

        System.out.println();
        System.out.println("Time consume --> " + runTime + " seconds");
        System.out.println();
    }
}
