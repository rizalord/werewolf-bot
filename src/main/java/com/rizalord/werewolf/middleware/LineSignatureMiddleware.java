package com.rizalord.werewolf.middleware;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public class LineSignatureMiddleware extends RootMiddleware {

    public void validate(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload
    ) {
        if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
            throw new RuntimeException("Invalid Signature Validation");
        }
    }

}
