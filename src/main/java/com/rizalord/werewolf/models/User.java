package com.rizalord.werewolf.models;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "line_user_id")
    private String line_user_id;

    @Column(name = "name")
    private String name;

    @Column(name = "room_id")
    private String room_id;

    @Column(name = "is_join", columnDefinition = "boolean default false")
    private boolean is_join = false;

    @Column(name = "is_out", columnDefinition = "boolean default false")
    private boolean is_out = false;

    @Column(name = "role_id")
    private int role_id;

    @Column(name = "poison_amount")
    private int poison_amount;

    @CreationTimestamp
    @Column(name="created_at")
    private Timestamp created_at;

    @CreationTimestamp
    @Column(name="updated_at")
    private Timestamp updated_at;
}
