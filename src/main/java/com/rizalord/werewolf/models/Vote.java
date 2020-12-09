package com.rizalord.werewolf.models;

import javax.persistence.*;

@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "room_id")
    private String room_id;

    @Column(name = "voter_id")
    private String voter_id;

    @Column(name = "voted_id")
    private String voted_id;
}
