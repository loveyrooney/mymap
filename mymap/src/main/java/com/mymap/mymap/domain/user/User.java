package com.mymap.mymap.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class User {
    @Id
    private long no;

    @Column(name="user_id",nullable = false)
    private String userId;

    @Column(name="password",nullable = false)
    private String password;
}
