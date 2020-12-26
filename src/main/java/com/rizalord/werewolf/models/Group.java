package com.rizalord.werewolf.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "groups")
@Getter @Setter @NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "groupId")
    private String groupId;

    @Column(name = "is_voting", columnDefinition = "boolean default false")
    private boolean is_voting = false;

    @Column(name = "is_inviting", columnDefinition = "boolean default true")
    private boolean is_inviting = true;

    @Column(name = "is_action", columnDefinition = "boolean default false")
    private boolean is_action = false;

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
