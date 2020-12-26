package com.rizalord.werewolf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.MembersIdsResponse;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.rizalord.werewolf.app.Config;
import com.rizalord.werewolf.app.EventsModel;
import com.rizalord.werewolf.middleware.LineSignatureMiddleware;
import com.rizalord.werewolf.models.Group;
import com.rizalord.werewolf.models.History;
import com.rizalord.werewolf.models.User;
import com.rizalord.werewolf.repository.GroupRepository;
import com.rizalord.werewolf.repository.HistoryRepository;
import com.rizalord.werewolf.repository.UserRepository;
import com.rizalord.werewolf.templates.MessageTemplate;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.nio.ch.IOUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
public class MainController {

    @Autowired
    @Qualifier("lineMessagingClient")
    protected LineMessagingClient lineMessagingClient;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private Config config;

    private List<Integer> miniRoles = new LinkedList<Integer>(Arrays.asList(1,2,4)) ;
    private List<Integer> largeRoles = new LinkedList<Integer>(Arrays.asList(1, 2, 3, 4)) ;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            // parsing event
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);

            eventsModel.getEvents().forEach((event)-> { eventHandler(event); });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*
    * This method is used to handle any event that get from line message
    * */
    private void eventHandler(Event event){


        // On Bot Added
        if (event instanceof FollowEvent) {
            final String userId = event.getSource().getSenderId();
            final String message = MessageTemplate.onFollowMessage;
            final TextMessage textMessage = new TextMessage(message);

            PushMessage pushMessage = new PushMessage(userId, textMessage);
            push(pushMessage);
        }

        // On Join Group Event
        else if (event instanceof JoinEvent) {
            final String groupId = event.getSource().getSenderId();
            final ArrayList<String> message = MessageTemplate.onJoinMessage;

            message.forEach((e) -> {
                TextMessage textMessage = new TextMessage(e);
                PushMessage pushMessage = new PushMessage(groupId, textMessage);

                push(pushMessage);
            } );

        }

        // Normal Message Event
        else if(event instanceof MessageEvent){

            if (event.getSource() instanceof GroupSource) {
                // Group Message

                String groupId = ((GroupSource) event.getSource()).getGroupId();
                String senderId = event.getSource().getSenderId();
                String userId = event.getSource().getUserId();

                MessageEvent messageEvent = (MessageEvent) event;
                MessageContent messageContent = (MessageContent) messageEvent.getMessage();

//                if (messageContent instanceof ImageMessageContent) {
//
//                    Group group = groupRepository.findGroupByLineId(groupId);
//
//                    if (group != null && !group.is_inviting()) {
//                        groupRepository.delete(group);
//                    }
//                }

                TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();


                String command = textMessageContent.getText();

                if (command.equals("@startgame")) {

                    String isBotAdded;

                    try {
                        isBotAdded = getProfile(userId).getDisplayName();

                    }catch (Exception e) {
                        pushBuilder(groupId, "Anda belum menambahkan Bot Werewolf sebagai teman. Tambahkan sebagai teman terlebih dahulu untuk melanjutkan.");

                        throw new RuntimeException(e);
                    }

                    System.out.println("PENCARIAN GROUP");
                    Group currentGroup = groupRepository.findGroupByLineId(groupId);

                    if (isBotAdded != null) {

                        if (currentGroup == null) {

                            System.out.println("Membuat group....");

                            Group group = new Group();
                            group.setGroupId(groupId);
                            group.set_voting(false);
                            group.set_inviting(true);
                            group.setCreated_at(new Timestamp(System.currentTimeMillis()));
                            group.setUpdated_at(new Timestamp(System.currentTimeMillis()));
                            group.setExpired_at(new Timestamp(System.currentTimeMillis()));

                            groupRepository.save(group);

                            System.out.println("Pembuatan group berhasil");

                            System.out.println("Membuat User...");

                            User groupCreator = new User();
                            groupCreator.setLine_user_id(userId);
                            groupCreator.setName(getProfile(userId).getDisplayName());
                            groupCreator.setRoom_id(group.getGroupId());
                            groupCreator.setGroup_id(group.getId());
                            groupCreator.set_join(true);
                            groupCreator.set_alive(true);
                            groupCreator.set_visibled(false);
                            groupCreator.set_poisoned(false);
                            groupCreator.set_visibling(false);
                            groupCreator.set_poisoning(false);
                            groupCreator.set_killing(false);
                            groupCreator.setWw_voter(0);
                            groupCreator.setPoison_amount(0);
                            groupCreator.setCreated_at(new Timestamp(System.currentTimeMillis()));
                            groupCreator.setUpdated_at(new Timestamp(System.currentTimeMillis()));

                            userRepository.save(groupCreator);

                            System.out.println("User berhasil diinsert");

                            pushBuilder(groupId, MessageTemplate.onGroupCreated);

                            setTimeout(() -> pushBuilder(groupId, MessageTemplate.gameStartIn30Sec),30000);

                            setTimeout(() -> pushBuilder(groupId, MessageTemplate.gameStartIn15Sec),45000);

                            setTimeout(() -> pushBuilder(groupId, MessageTemplate.gameStartIn7Sec),53000);

                            setTimeoutSync(() -> {

                                if (userRepository.countUsersByGroupId(group.getId()) >= 6) {

                                    group.set_inviting(false);
                                    groupRepository.save(group);

                                    pushBuilder(groupId, "Game Dimulai!");

                                    ArrayList<User> players = userRepository.findUsersByGroupId(group.getId());
                                    ArrayList<User> shuffledPlayers = players;
                                    Collections.shuffle(shuffledPlayers);

                                    List<Integer> roles = players.size() <= 8 ? miniRoles : largeRoles;
                                    List<Integer> tmpRoles = new LinkedList<Integer>(Collections.emptyList());

                                    shuffledPlayers.forEach((e) -> {

                                        if (roles.size() > 0) {
                                            e.setRole_id(roles.get(0));
                                            if (roles.get(0) == 3) e.setPoison_amount(1);
                                            userRepository.save(e);
                                            tmpRoles.add(roles.get(0));
                                            roles.remove(0);
                                        }

                                        String userRole = getRoleName(e.getRole_id());

                                        pushBuilder(e.getLine_user_id(), "Anda mendapat role " + userRole);
                                    });

                                    group.set_inviting(false);
                                    group.set_action(true);
                                    groupRepository.save(group);

                                    boolean isGameEnd = false;

                                    // Game Startpoint
                                    for (int i = 0; i < 10; i++) {

                                        group.set_voting(false);
                                        group.set_action(true);
                                        groupRepository.save(group);

                                        userRepository.resetSpecialRoleState(group.getId());
                                        userRepository.refreshAll();

                                        players = userRepository.findUsersByGroupId(group.getId());

                                        ImageMessage imageMessage = new ImageMessage("https://raw.githubusercontent.com/rizalord/werewolf-bot-assets/main/night-preview.png", "https://raw.githubusercontent.com/rizalord/werewolf-bot-assets/main/night-preview.png");

                                        pushBuilder(groupId , imageMessage);
                                        pushBuilder(groupId, "Malam telah tiba");


                                        // send option to special roles
                                        if (group.is_action()) {

                                            players.forEach((player) -> {

                                                if (player.getRole_id() == 1 && player.is_alive() && !player.is_visibling()) {
                                                    ArrayList<User> notVisibledPlayers = userRepository.findNotVisibledYetUsersByGroupId(group.getId());

                                                    String text = "Silahkan pilih pemain untuk dilihat\n";

                                                    if (notVisibledPlayers.size() == 0) text = "Tidak ada pemain yang bisa dilihat";
                                                    else {
                                                        for (User user : notVisibledPlayers) {
                                                            text += "\n" + (notVisibledPlayers.indexOf(user) + 1) + ". " + user.getName() ;
                                                        }
                                                    }

                                                    pushBuilder(player.getLine_user_id() , text);
                                                }

                                                else if (player.getRole_id() == 2 && player.is_alive() && !player.is_killing()) {
                                                    ArrayList<User> notKilledPlayers = userRepository.findNotKilledYetUsersByGroupId(group.getId());

                                                    String text = "Silahkan pilih pemain untuk dibunuh\n";

                                                    if (notKilledPlayers.size() == 0) text = "Tidak ada pemain yang bisa dibunuh";
                                                    else {
                                                        for (User user : notKilledPlayers) {
                                                            text += "\n" +  (notKilledPlayers.indexOf(user) + 1) + ". " + user.getName();
                                                        }
                                                    }

                                                    pushBuilder(player.getLine_user_id() , text);
                                                }

                                                else if (player.getRole_id() == 3 && player.is_alive() && !player.is_poisoning()) {

                                                    if (player.getPoison_amount() > 0) {
                                                        ArrayList<User> notPoisonedPlayers = userRepository.findNotPoisonedYetUsersByGroupId(group.getId());

                                                        StringBuilder text = new StringBuilder("Silahkan pilih pemain untuk diracun\n");

                                                        if (notPoisonedPlayers.size() == 0)
                                                            text = new StringBuilder("Tidak ada pemain yang bisa diracun");
                                                        else {
                                                            for (User user : notPoisonedPlayers) {
                                                                text.append("\n").append(notPoisonedPlayers.indexOf(user) + 1).append(". ").append(user.getName());
                                                            }
                                                        }

                                                        pushBuilder(player.getLine_user_id(), text.toString());
                                                    } else {
                                                        pushBuilder(player.getLine_user_id(), "Racun sudah habis, silahkan tunggu hari berikutnya");
                                                    }
                                                }

                                                else if (player.getRole_id() == 4 && player.is_alive() && !player.is_guarding()) {

                                                    ArrayList<User> notVisibledPlayers = userRepository.findStillAliveUsersByGroupId(group.getId());

                                                    String text = "Silahkan pilih pemain untuk dilindungi\n";

                                                    if (notVisibledPlayers.size() == 0) text = "Tidak ada pemain yang bisa dilindungi";
                                                    else {
                                                        for (User user : notVisibledPlayers) {
                                                            text += "\n" + (notVisibledPlayers.indexOf(user) + 1) + ". " + user.getName() ;
                                                        }
                                                    }

                                                    pushBuilder(player.getLine_user_id() , text);
                                                }
                                            });
                                        }


                                        setTimeoutSync(() -> pushBuilder(groupId, "Silahkan tunggu tindakan pemain di malam hari"), 3000);

                                        setTimeoutSync(() -> pushBuilder(groupId, "Malam akan berakhir dalam 15 detik"), 10000);

                                        setTimeoutSync(() -> {

                                            group.set_action(false);
                                            group.set_voting(true);

                                        }, 15000);

                                        pushBuilder(groupId, "Pagi telah tiba");

                                        setTimeoutSync(() -> pushBuilder(groupId, "..."), 3000);

                                        historyRepository.refresh();
                                        ArrayList<History> histories = historyRepository.findHistoryByGroupId(group.getId());

                                        setTimeoutSync(() -> {
                                            if (histories.size() == 0) {
                                                pushBuilder(groupId, "Malam yang damai.. ");
                                            } else {
                                                String text = "Kejadian Malam Ini: \n";
                                                int number = 1;
                                                for (History history: histories) {

                                                    String tempText = "\n"  + number + ". " + history.getMessage();

                                                    if (history.getAction_id() == 2) {

                                                        int targetId = history.getTarget_user_id();
                                                        User targetUser = userRepository.findUserById(targetId);

                                                        if (targetUser.is_guarded()) {
                                                            targetUser.set_guarded(false);
                                                            tempText = "\n"  + number + ". " + targetUser.getName() + " dilindungi oleh Penjaga";
                                                        }
                                                        else targetUser.set_alive(false);

                                                        userRepository.save(targetUser);

                                                    }

                                                    text = text + tempText;
                                                    number++;
                                                }

                                                userRepository.updateGuardingToNotGuarding(group.getId());
                                                userRepository.refreshAll();

                                                pushBuilder(groupId, text);
                                            }
                                        }, 3000);

                                        userRepository.refreshAll();

                                        if (userRepository.isWerewolfStillAlive(group.getId())) {

                                            if (userRepository.isWerewolfWin(group.getId())) {
                                                User ww = userRepository.findWerewolf(group.getId());
                                                pushBuilder(groupId, ww.getName() +  " (werewolf) menang setelah membunuh semua nya");
                                                isGameEnd = true;
                                                roles.addAll(tmpRoles);
                                                tmpRoles.clear();
                                                userRepository.endGame1ByGroupId(group.getId());
                                                groupRepository.endGame2ByGroupId(group.getId());
                                                historyRepository.endGame3ByGroupId(group.getId());
                                                break;
                                            }

                                        } else {
                                            pushBuilder(groupId, "Penduduk (Baik) Memenangkan Permainan!");
                                            isGameEnd = true;
                                            roles.addAll(tmpRoles);
                                            tmpRoles.clear();
                                            userRepository.endGame1ByGroupId(group.getId());
                                            groupRepository.endGame2ByGroupId(group.getId());
                                            historyRepository.endGame3ByGroupId(group.getId());
                                            break;
                                        }


                                        setTimeoutSync(() -> pushBuilder(groupId, "Silahkan berdiskusi selama 1,5 menit"), 3000);
                                        setTimeoutSync(() -> pushBuilder(groupId, "Diskusi akan berakhir dalam 30 detik"), 60000);
                                        setTimeoutSync(() -> pushBuilder(groupId, "Waktu diskusi telah selesai!"), 30000);
                                        setTimeoutSync(() -> pushBuilder(groupId, "Silahkan lakukan voting"), 1000);

                                        group.set_voting(true);
                                        group.set_action(false);
                                        groupRepository.save(group);
                                        userRepository.updateAllUserVoteState(group.getId(), false);
                                        userRepository.refreshAll();
                                        players = userRepository.findUsersByGroupId(group.getId());

                                        // send voting option to users
                                        if (group.is_voting()) {

                                            players.forEach((player) -> {

                                                if (player.is_alive() && !player.is_voting()) {
                                                    ArrayList<User> aliveUsers = userRepository.findStillAliveUsersByGroupIdExceptSelfId(group.getId(), player.getId());

                                                    String text = "Silahkan pilih pemain untuk divote\n";

                                                    if (aliveUsers.size() == 0) text = "Tidak ada pemain yang bisa divote";
                                                    else {
                                                        for (User user : aliveUsers) {
                                                            text += "\n" +  (aliveUsers.indexOf(user) + 1) + ". " + user.getName() ;
                                                        }
                                                    }

                                                    pushBuilder(player.getLine_user_id() , text);
                                                }
                                            });
                                        }

                                        setTimeoutSync(() -> pushBuilder(groupId, "Voting akan berakhir dalam 15 detik"), 15000);
                                        setTimeoutSync(() -> pushBuilder(groupId, "..."), 15000);
                                        setTimeoutSync(() -> {
                                            userRepository.updateVotingToNotVoting(group.getId());
                                            userRepository.refreshAll();
                                            User votedUser = userRepository.findVotedUsers(group.getId());
                                            String text = "Hasil vote \n\n";

                                            if (votedUser == null) {

                                                text += "Tidak ada pemain yang divote";
                                                pushBuilder(groupId , text);

                                            }else {

                                                String playerRole = getRoleName(votedUser.getRole_id());

                                                text += votedUser.getName() + " (" + playerRole + ")" +  " telah divote.";
                                                pushBuilder(groupId , text);

                                                votedUser.setWw_voter(0);
                                                votedUser.set_alive(false);
                                                userRepository.save(votedUser);
                                                userRepository.resetVoter(group.getId());
                                            }

                                            historyRepository.deleteHistoryByGroupId(group.getId());
                                            historyRepository.refresh();

                                        }, 3000);

                                        userRepository.refreshAll();

                                        if (userRepository.isWerewolfStillAlive(group.getId())) {

                                            if (userRepository.isWerewolfWin(group.getId())) {
                                                pushBuilder(groupId, "Werewolf (Jahat) Memenangkan Permainan!");
                                                isGameEnd = true;
                                                roles.addAll(tmpRoles);
                                                tmpRoles.clear();
                                                userRepository.endGame1ByGroupId(group.getId());
                                                groupRepository.endGame2ByGroupId(group.getId());
                                                historyRepository.endGame3ByGroupId(group.getId());
                                                break;
                                            }

                                        } else {
                                            pushBuilder(groupId, "Penduduk menang setelah menemukan werewolf nya sudah wafat. Penduduk pun hidup bahagia");
                                            isGameEnd = true;
                                            roles.addAll(tmpRoles);
                                            tmpRoles.clear();
                                            userRepository.endGame1ByGroupId(group.getId());
                                            groupRepository.endGame2ByGroupId(group.getId());
                                            historyRepository.endGame3ByGroupId(group.getId());
                                            break;
                                        }

                                    }
                                    // Game Endpoint

                                    if (!isGameEnd) {
                                        roles.addAll(tmpRoles);
                                        tmpRoles.clear();
                                        userRepository.refreshAll();
                                        userRepository.endGame1ByGroupId(group.getId());
                                        groupRepository.endGame2ByGroupId(group.getId());
                                        historyRepository.endGame3ByGroupId(group.getId());
                                        pushBuilder(groupId, "Batas round melebihi batas, permainan diberhentikan");
                                    }

                                } else {
                                    pushBuilder(groupId, MessageTemplate.endTimerGroupCreate);
                                    userRepository.deleteUserByGroupId(group.getId());
                                    groupRepository.deleteGroupById(group.getId());
                                }

                            }, 60000);

                        } else {

                        }

                    }

                } else if (command.equals("@join")) {

                    Group group = groupRepository.findGroupByLineId(groupId);

                    if ( group != null) {

                        if (userRepository.findUserByLineUserId(userId) == null) {

                            try {
                                String userDisplayName = getProfile(userId).getDisplayName();

                                User user = new User();
                                user.setLine_user_id(userId);
                                user.setName(userDisplayName);
                                user.setRoom_id(group.getGroupId());
                                user.setGroup_id(group.getId());
                                user.set_join(true);
                                user.set_alive(true);
                                user.set_visibled(false);
                                user.set_poisoned(false);
                                user.set_visibling(false);
                                user.set_poisoning(false);
                                user.set_killing(false);
                                user.setRole_id(0);
                                user.setWw_voter(0);
                                user.setPoison_amount(0);
                                user.setCreated_at(new Timestamp(System.currentTimeMillis()));
                                user.setUpdated_at(new Timestamp(System.currentTimeMillis()));

                                userRepository.save(user);
                                pushBuilder(groupId, userDisplayName + " telah bergabung");

                            }catch (Exception e){

                                pushBuilder(groupId, "Anda belum menambahkan Bot Werewolf sebagai teman. Tambahkan sebagai teman terlebih dahulu untuk melanjutkan.");

                                throw new RuntimeException(e);
                            }

                        } else {
                            pushBuilder(groupId, "Anda sudah bergabung ke permainan");
                        }

                    } else {
                        pushBuilder(groupId, "Tidak ada permainan");
                    }

                }



            } else {
                // Private Message

                MessageEvent messageEvent = (MessageEvent) event;
                TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();

                String message = textMessageContent.getText();
                String senderId = event.getSource().getSenderId();
                String userId = event.getSource().getUserId();
                String replyToken = ((MessageEvent) event).getReplyToken();
                User user = userRepository.findUserByLineUserId(userId);

                // Jika user ada di dalam room
                if(user != null) {

                    Optional<Group> optionalGroup = groupRepository.findById(user.getGroup_id());

                    // Jika group ada
                    if (optionalGroup.isPresent()){

                        Group group = optionalGroup.get();

                        if (group.is_action()){

                            userRepository.refreshAll();

                            if (user.getRole_id() == 1 && !user.is_visibling()) {
                                ArrayList<User> players = userRepository.findNotVisibledYetUsersByGroupId(group.getId());

                                try {
                                    User playerChoose = players.get(Integer.parseInt(message) - 1);

                                    String role = getRoleName(playerChoose.getRole_id());

                                    String text = playerChoose.getName() + " adalah " + role;

                                    replyText(replyToken , text);

                                    playerChoose.set_visibled(true);
                                    user.set_visibling(true);

                                    List<User> savedUsers = new LinkedList<User>(Arrays.asList(user, playerChoose));
                                    userRepository.saveAll(savedUsers);

                                }catch(Exception e) {
                                    replyText(replyToken, "Pesan tidak diketahui, silahkan coba lagi nanti");
                                    throw new RuntimeException(e);

                                }

                            }

                            else if (user.getRole_id() == 2 && !user.is_killing()) {
                                ArrayList<User> players = userRepository.findNotKilledYetUsersByGroupId(group.getId());

                                try {
                                    User playerChoose = players.get(Integer.parseInt(message) - 1);

                                    int index = 0;
                                    for (User player : players) {
                                        System.out.println(index + ". " + player.getName());
                                        index++;
                                    }
                                    System.out.println("Dipilih index " + (Integer.parseInt(message) - 1) + " dgn nma " + playerChoose.getName());

                                    String playerRole = getRoleName(playerChoose.getRole_id());

                                    String text = playerChoose.getName() + " kamu bunuh";

                                    replyText(replyToken , text);

                                    user.set_killing(true);

                                    List<User> savedUsers = new LinkedList<User>(Arrays.asList(user, playerChoose));
                                    userRepository.saveAll(savedUsers);

                                    setHistory( playerChoose.getName() + " (" + playerRole + ") telah dibunuh oleh werewolf", user.getRole_id(), user.getRole_id(), playerChoose.getId(),  group.getId(), group.getGroupId());

                                }catch(Exception e) {
                                    replyText(replyToken, "Pesan tidak diketahui, silahkan coba lagi nanti");
                                    throw new RuntimeException(e);

                                }

                            }

                            else if (user.getRole_id() == 3 && !user.is_poisoning() && user.getPoison_amount() > 0) {
                                ArrayList<User> players = userRepository.findNotPoisonedYetUsersByGroupId(group.getId());

                                try {
                                    User playerChoose = players.get(Integer.parseInt(message) - 1);

                                    int index = 0;
                                    for (User player : players) {
                                        System.out.println(index + ". " + player.getName());
                                        index++;
                                    }
                                    System.out.println("Dipilih index " + (Integer.parseInt(message) - 1) + " dgn nma " + playerChoose.getName());

                                    String playerRole = getRoleName(playerChoose.getRole_id());

                                    String text = playerChoose.getName() + " telah kamu racun";

                                    replyText(replyToken , text);

                                    playerChoose.set_alive(false);
                                    user.set_poisoning(true);
                                    user.setPoison_amount(0);

                                    List<User> savedUsers = new LinkedList<User>(Arrays.asList(user, playerChoose));
                                    userRepository.saveAll(savedUsers);

                                    setHistory(playerChoose.getName() + " (" + playerRole + ") telah diracun oleh Penyihir", user.getRole_id(), user.getRole_id(), playerChoose.getId() , group.getId(), group.getGroupId());

                                }catch(Exception e) {
                                    replyText(replyToken, "Pesan tidak diketahui, silahkan coba lagi nanti");
                                    throw new RuntimeException(e);

                                }

                            }

                            else if (user.getRole_id() == 4 && !user.is_guarding()) {
                                ArrayList<User> players = userRepository.findStillAliveUsersByGroupId(group.getId());

                                try {
                                    User playerChoose = players.get(Integer.parseInt(message) - 1);

                                    String playerName = user.getId() == playerChoose.getId()
                                            ? "diri sendiri"
                                            : playerChoose.getName();

                                    String text = "Anda melindungi " +  playerName;

                                    replyText(replyToken , text);

                                    if (user.getId() == playerChoose.getId()) {

                                        user.set_guarded(true);
                                        user.set_guarding(true);

                                        userRepository.saveAndFlush(user);

                                        System.out.println("LINDUNGI DIRI SENDIRI - SAVE - " + user.is_guarded() );

                                    } else {

                                        playerChoose.set_guarded(true);
                                        user.set_guarding(true);

                                        List<User> savedUsers = new LinkedList<User>(Arrays.asList(user, playerChoose));

                                        userRepository.saveAll(savedUsers);
                                        userRepository.flush();

                                        System.out.println("LINDUNGI ORANG LAIN - SAVE - " + playerChoose.is_guarded() );

                                    }


                                }catch(Exception e) {
                                    replyText(replyToken, "Pesan tidak diketahui, silahkan coba lagi nanti");
                                    throw new RuntimeException(e);

                                }

                            }



                        }else if (group.is_voting() && !user.is_voting()) {

                            ArrayList<User> players = userRepository.findStillAliveUsersByGroupIdExceptSelfId(group.getId(), user.getId());

                            try {
                                User playerChoose = players.get(Integer.parseInt(message) - 1);

                                String text = "Anda melakukan vote terhadap " + playerChoose.getName();

                                replyText(replyToken , text);

                                userRepository.incrementVoter(group.getId(), playerChoose.getId());
                                user.set_voting(true);

                                userRepository.save(user);
                                userRepository.flush();

                            }catch(Exception e) {
                                replyText(replyToken, "Pesan tidak diketahui, silahkan coba lagi nanti");
                                throw new RuntimeException(e);

                            }

                        } else { }

                    } else { }

                }else {

                    switch (message) {
                        case "commands":
                            replyText(messageEvent.getReplyToken(), MessageTemplate.listCommands);
                            break;
                        case "info":
                            replyText(messageEvent.getReplyToken(), MessageTemplate.info);
                            break;
                        case "howtoplay":
                            replyText(messageEvent.getReplyToken(), MessageTemplate.howtoplay);
                            break;
                        case "rules":
                            replyText(messageEvent.getReplyToken(), MessageTemplate.rules);
                            break;
                        case "roles":
                            pushRoles(messageEvent.getReplyToken());
                            break;
                        default:
                            replyText(messageEvent.getReplyToken(), MessageTemplate.unknownCommand);
                            break;
                    }

                }


//                MessageEvent messageEvent = (MessageEvent) event;
//                TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();
//                replyText(messageEvent.getReplyToken(), textMessageContent.getText());
            }

        } else  { }
    }


    /*
    *   From here to below are standard method to help connect with Line API
    * */
    private void reply(ReplyMessage replyMessage){
        try{
            lineMessagingClient.replyMessage(replyMessage).get();
        }catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    private void replyText(String replyToken, String message){
        TextMessage textMessage = new TextMessage(message);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);
        reply(replyMessage);
    }

    private void replyText(String replyToken, FlexMessage message){
        ReplyMessage replyMessage = new ReplyMessage(replyToken, message);
        reply(replyMessage);
    }

    private UserProfileResponse getProfile(String userId){
        try{
            return lineMessagingClient.getProfile(userId).get();
        }catch (InterruptedException | ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    private void push(PushMessage pushMessage){
        try {
            lineMessagingClient.pushMessage(pushMessage).get();
        }catch (InterruptedException| ExecutionException e){
            throw new RuntimeException(e);
        }
    }

    private void pushBuilder(String targetId, String message){

        TextMessage textMessage = new TextMessage(message);
        PushMessage pushMessage = new PushMessage(targetId, textMessage);
        push(pushMessage);

    }

    private void pushBuilder(String targetId, ImageMessage imageMessage){

        PushMessage pushMessage = new PushMessage(targetId, imageMessage);
        push(pushMessage);

    }

    private void pushBuilder(String targetId, VideoMessage videoMessage){

        PushMessage pushMessage = new PushMessage(targetId, videoMessage);
        push(pushMessage);

    }

    private void pushStartButton(String groupId) {
        try{
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("start_game_button.json"));

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

            PushMessage pushMessage = new PushMessage(groupId, new FlexMessage("start", flexContainer));
            push(pushMessage);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    private void pushRoles(String replyToken) {
        try{
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("roles.json"));

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

            replyText(replyToken, new FlexMessage("Daftar Roles", flexContainer));
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    // Option Helper
    private void setTimeoutSync(Runnable runnable, int delay) {
        try {
            Thread.sleep(delay);
            runnable.run();
        }
        catch (Exception e){
            System.err.println(e);
        }
    }

    private void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    private void setHistory(String message, int roleId, int actionId, int targetUserId, int groupId, String roomId) {

        History history = new History();
        history.setGroup_id(groupId);
        history.setRoom_id(roomId);
        history.setRole_id(roleId);
        history.setAction_id(actionId);
        history.setTarget_user_id(targetUserId);
        history.setMessage(message);
        history.setCreated_at(new Timestamp(System.currentTimeMillis()));
        history.setUpdated_at(new Timestamp(System.currentTimeMillis()));
        historyRepository.save(history);

    }

    private String getRoleName(int roleId) {
        return roleId == 0
                ? "Villager"
                : roleId == 1
                ? "Penerawang"
                : roleId == 2
                ? "Werewolf"
                : roleId == 3
                ? "Penyihir"
                : "Penjaga";
    }

}
