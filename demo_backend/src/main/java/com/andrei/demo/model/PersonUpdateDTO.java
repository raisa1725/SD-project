package com.andrei.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonUpdateDTO {

    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    private String name;

    private Integer age;

    @Email(message = "Email must be valid")
    private String email;

    private PersonRole role;
}