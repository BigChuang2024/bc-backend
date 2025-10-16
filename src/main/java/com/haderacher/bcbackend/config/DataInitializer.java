package com.haderacher.bcbackend.config;

import com.haderacher.bcbackend.model.Role;
import com.haderacher.bcbackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createRoleIfNotExists("ROLE_STUDENT");
        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_RECRUITER");
    }

    private void createRoleIfNotExists(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}