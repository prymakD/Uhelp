package com.tehcman.input_final_destination.handlers;

import com.tehcman.input_final_destination.SendMessage_factories.CacheFactoryHost;
import com.tehcman.input_final_destination.SendMessage_factories.CacheFactoryRefugee;
import com.tehcman.sendmessage.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class SaveToCacheIHandler implements IHandler<Message> {

    private final MessageSender messageSender;
    private final CacheFactoryRefugee cacheFactoryRefugee;
    private final CacheFactoryHost cacheFactoryHost;
    private final InitDataAndStatusHandler initDataAndStatusHandler;

    @Autowired
    public SaveToCacheIHandler(MessageSender messageSender, CacheFactoryRefugee cacheFactoryRefugee, CacheFactoryHost cacheFactoryHost, InitDataAndStatusHandler initDataAndStatusHandler) {
        this.messageSender = messageSender;
        this.cacheFactoryRefugee = cacheFactoryRefugee;
        this.cacheFactoryHost = cacheFactoryHost;
        this.initDataAndStatusHandler = initDataAndStatusHandler;
    }



    @Override
    public void handle(Message message) {

//        initDataAndStatusHandler.handle(message); and then if statements
        SendMessage newMsg = cacheFactoryRefugee.createSendMessage(message);
        messageSender.messageSend(newMsg);
    }


}
