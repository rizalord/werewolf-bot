package com.rizalord.werewolf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.rizalord.werewolf.app.EventsModel;
import com.rizalord.werewolf.repository.UserRepository;
import com.rizalord.werewolf.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class MainController {

    @Autowired
    @Qualifier("lineMessagingClient")
    protected LineMessagingClient lineMessagingClient;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
//            new LineSignatureMiddleware().validate(xLineSignature, eventsPayload);

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
            final String message = MessageService.onFollowMessage;
            final TextMessage textMessage = new TextMessage(message);

            PushMessage pushMessage = new PushMessage(userId, textMessage);
            push(pushMessage);
        }

        // On Join Group Event
        else if (event instanceof JoinEvent) {
            final String groupId = event.getSource().getSenderId();
            final String message = MessageService.onJoinMessage;
            final TextMessage textMessage = new TextMessage(message);

            PushMessage pushMessage = new PushMessage(groupId, textMessage);
            push(pushMessage);
        }

        // Normal Message Event
        else if(event instanceof MessageEvent){

            if (event.getSource() instanceof GroupSource) {
                // Group

            } else {
                // One on One

                MessageEvent messageEvent = (MessageEvent) event;
                TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();
                replyText(messageEvent.getReplyToken(), textMessageContent.getText());
            }

        }
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

}
