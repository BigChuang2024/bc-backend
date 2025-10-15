package com.haderacher.bcbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User implements UserDetails { // 2. 实现 UserDetails 接口

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = true, length = 100)
    private String email;

    private String phone;

    @Column(name = "is_enabled", nullable = false) // 明确列名，避免与方法名冲突
    private boolean enabled = true;

    @Column(name = "is_account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "is_account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 返回分配给用户的权限集合。这是最核心的方法。
     * Spring Security 会根据这里的权限来决定用户是否有权访问某个资源。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将 Set<Role> 转换成 Set<GrantedAuthority>
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 账户是否未过期。
     * 如果你的系统没有账户过期的功能，直接返回 true 即可。
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    /**
     * 账户是否未被锁定。
     * 如果你没有锁定账户的功能（例如，多次密码错误后锁定），直接返回 true。
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    /**
     * 用户凭证（密码）是否未过期。
     * 如果你没有密码过期的策略，直接返回 true。
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    /**
     * 账户是否启用。
     * 这个可以直接使用我们已有的 enabled 字段。
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}