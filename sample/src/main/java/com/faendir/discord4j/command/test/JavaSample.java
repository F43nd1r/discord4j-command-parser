package com.faendir.discord4j.command.test;


import com.faendir.discord4j.command.annotation.ApplicationCommand;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "Sam", subCommand = true)
public class JavaSample {
    private final String name;
    private final int age;
    private final Boolean male;
    private final Companies companyName;

    public JavaSample(@NotNull String name, int age, Boolean male, Companies companyName) {
        this.name = name;
        this.age = age;
        this.male = male;
        this.companyName = companyName;
    }
}

