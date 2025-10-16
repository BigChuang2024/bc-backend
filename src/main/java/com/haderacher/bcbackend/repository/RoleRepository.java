package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // 根据角色名称查找角色
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}