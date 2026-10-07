package com.fitnessops.modules.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Vai trò nhân viên. Danh mục cố định, khởi tạo bằng migration. */
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /** Vai trò chỉ được đăng nhập trên máy quầy đã đăng ký (lễ tân). */
    @Column(name = "requires_counter_device", nullable = false)
    private boolean requiresCounterDevice;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
