package com.rizalord.werewolf.templates;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageTemplate {
    public static ArrayList<String> onJoinMessage = new ArrayList<String>(
            Arrays.asList(
                    "Bot Werewolf telah bergabung ke dalam group. Ketik @startgame untuk memulai permainan dan ketik @join untuk bergabung ke permainan"
            )
    )  ;
    public static String onFollowMessage = "Untuk melihat perintah apa saja ketik \"commands\" (tanpa tanda petik)";
    public static String onGroupCreated = "Room telah dibuat, untuk bergabung dalam permainan ketik pesan @join";
    public static String endTimerGroupCreate = "Permainan tidak dapat dimulai karena pemain terlalu sedikit";
    public static String gameStartIn30Sec = "Game akan dimulai dalam 30 detik lagi";
    public static String gameStartIn15Sec = "Game akan dimulai dalam 15 detik lagi";
    public static String gameStartIn7Sec = "Game akan dimulai dalam 7 detik lagi";
    public static String listCommands = "Daftar Perintah \n\n" +
            "info - Info bot\n" +
            "roles - Daftar role dalam permainan\n" +
            "howtoplay - Cara bermain\n" +
            "rules - Aturan Bermain";
    public static String info = "Bot Werewolf adalah bot yang digunakan untuk bermain game bersama teman - teman satu " +
            "group. Bot ini sendiri masih dalam tahap pengembangan dan mungkin membutuhkan beberapa perbaikan untuk " +
            "kedepannya.";
    public static String howtoplay = "Untuk cara bermain:\n\n" +
            "1. Undang Bot Werewolf ke dalam group\n" +
            "2. Ketik @startgame untuk mulai permainan\n" +
            "3. Ketik @join bagi pemain lain untuk bergabung dalam permainan\n" +
            "4. Ikuti alur permainan";
    public static String rules = "Aturan Permainan: \n\n" +
            "1. Mininal terdapat 6 pemain untuk memulai permainan\n" +
            "2. Selama dalam permainan di group dilarang untuk mengirimkan gambar\n" +
            "3. Dilarang untuk membagikan role anda kepada pemain lain melalui chat pribadi\n" +
            "4. Batas waktu permainan adalah 10 ronde (10x pagi & malam), melebihi itu secara otomatis akan keluar dari permaianan";
    public static String unknownCommand = "Perintah tidak diketahui, Untuk melihat perintah apa saja ketik \"commands\" (tanpa tanda petik)";


}
