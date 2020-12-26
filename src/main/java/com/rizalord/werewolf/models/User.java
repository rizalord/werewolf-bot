package com.rizalord.werewolf.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "group_id")
    private int group_id;

    @Column(name = "is_join", columnDefinition = "boolean default false")
    private boolean is_join = false;

    @Column(name = "is_alive", columnDefinition = "boolean default true", nullable = false)
    private boolean is_alive = true;

    @Column(name = "is_visibled", columnDefinition = "boolean default false")
    private boolean is_visibled = false;

    @Column(name = "is_poisoned", columnDefinition = "boolean default false")
    private boolean is_poisoned = false;

    @Column(name = "is_visibling", columnDefinition = "boolean default false")
    private boolean is_visibling = false;

    @Column(name = "is_poisoning", columnDefinition = "boolean default false")
    private boolean is_poisoning = false;

    @Column(name = "is_killing", columnDefinition = "boolean default false")
    private boolean is_killing = false;

    @Column(name = "is_voting", columnDefinition = "boolean default false")
    private boolean is_voting = false;

    @Column(name = "is_guarded", columnDefinition = "boolean default false")
    private boolean is_guarded = false;

    @Column(name = "is_guarding", columnDefinition = "boolean default false")
    private boolean is_guarding = false;

    @Column(name = "ww_voter")
    private int ww_voter = 0;

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
