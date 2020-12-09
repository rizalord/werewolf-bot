package com.rizalord.werewolf.middleware;

import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class RootMiddleware {

    @Autowired
    @Qualifier("lineSignatureValidator")
    protected LineSignatureValidator lineSignatureValidator;

    public void run(){
        //
    };
}
