package com.rizalord.werewolf.models;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "groupId")
    private int groupId;

    @CreationTimestamp
    @Column(name="created_at")
    private Timestamp created_at;

    @CreationTimestamp
    @Column(name="updated_at")
    private Timestamp updated_at;

    @CreationTimestamp
    @Column(name="expired_at")
    private Timestamp expired_at;
}
