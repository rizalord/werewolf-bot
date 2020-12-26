package com.rizalord.werewolf.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "histories")
@Getter
@Setter
@NoArgsConstructor
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "role_id")
    private int role_id;

    @Column(name = "action_id")
    private int action_id;

    @Column(name = "target_user_id")
    private int target_user_id;

    @Column(name = "message")
    private String message;

    @Column(name = "room_id")
    private String room_id;

    @Column(name = "group_id")
    private int group_id;

    @CreationTimestamp
    @Column(name="created_at")
    private Timestamp created_at;

    @CreationTimestamp
    @Column(name="updated_at")
    private Timestamp updated_at;
}
