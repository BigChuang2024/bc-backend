package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.dto.LoginStudentRequest;
import com.haderacher.bcbackend.entity.aggregates.student.Student;
import com.haderacher.bcbackend.service.StudentService;
import com.haderacher.bcbackend.dto.RegisterStudentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class StudentController {

    private final StudentService studentService;

    StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/students")
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
        return null;
    }

    @PostMapping("/students/login")
    public ResponseEntity<Object> studentLogin(@RequestBody LoginStudentRequest loginStudentRequest) {
        String token = studentService.loginStudentAndGetToken(loginStudentRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/students/register")
    public ResponseEntity<Object> studentRegister(@RequestBody RegisterStudentRequest registerStudentRequest) {
        String  token = studentService.registerStudentAndGetToken(registerStudentRequest);
        return ResponseEntity.status(201)
                .body(token);
    }
}

