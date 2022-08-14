package com.tehcman.input_final_destination.handlers;

import com.tehcman.cahce.Cache;
import com.tehcman.entities.Phase;
import com.tehcman.entities.Status;
import com.tehcman.entities.User;
import com.tehcman.input_final_destination.SendMessage_factories.CacheFactoryHost;
import com.tehcman.input_final_destination.SendMessage_factories.CacheFactoryRefugee;
import com.tehcman.sendmessage.MessageSender;
import com.tehcman.services.BuildButtonsService;
import com.tehcman.services.IBuildSendMessageService;
import com.tehcman.services.keyboards.AddSkipButtonKeyboardRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class SaveToCacheIHandler implements IHandler<Message> {
    private final IBuildSendMessageService ibuildSendMessageService;
    private final MessageSender messageSender;
    private final CacheFactoryRefugee cacheFactoryRefugee;
    private final CacheFactoryHost cacheFactoryHost;
    private final InitDataAndStatusHandler initDataAndStatusHandler;
    private final Cache<User> userCache;
    private BuildButtonsService buildButtonsService;

    @Autowired
    public SaveToCacheIHandler(IBuildSendMessageService ibuildSendMessageService, MessageSender messageSender, CacheFactoryRefugee cacheFactoryRefugee, CacheFactoryHost cacheFactoryHost, InitDataAndStatusHandler initDataAndStatusHandler, Cache<User> userCache) {
        this.ibuildSendMessageService = ibuildSendMessageService;
        this.messageSender = messageSender;
        this.cacheFactoryRefugee = cacheFactoryRefugee;
        this.cacheFactoryHost = cacheFactoryHost;
        this.initDataAndStatusHandler = initDataAndStatusHandler;
        this.userCache = userCache;
    }


    @Override
    public void handle(Message message) {
        User user = userCache.findBy(message.getChatId());
        if (user == null) {
            messageSender.messageSend(initDataAndStatusHandler.createSendMessage(message));
        } else if (user.getPhase() == Phase.STATUS){
            registerRestUserData(user, message);

        }else{
            if (user.getStatus() == Status.REFUGEE) {
                SendMessage newMsg = cacheFactoryRefugee.createSendMessage(message);
                messageSender.messageSend(newMsg);
            } else {
                SendMessage newMsg = cacheFactoryHost.createSendMessage(message);
                messageSender.messageSend(newMsg);
            }
        }
    }

    //fixme it doesnt change the keyboard
    private SendMessage registerRestUserData(User user, Message message) {
        switch (user.getPhase()) {
            case STATUS:
                if (message.getText().equals("Searching Accommodation")) {
                    user.setStatus(Status.REFUGEE);
                    user.setPhase(Phase.NAME);

                    this.buildButtonsService = new BuildButtonsService(new AddSkipButtonKeyboardRow());

                    //TODO careful with this part!
                    SendMessage newMessage = cacheFactoryRefugee.createSendMessage(message);
                    messageSender.messageSend(newMessage);
//                    SendMessage sendMessage = ibuildSendMessageService.createHTMLMessage(message.getChatId().toString(), "Type your name or SKIP if you want to set your default Telegram name", buildButtonsService.getMainMarkup());
                    return cacheFactoryRefugee.createSendMessage(message);
                } else if (message.getText().equals("Providing Accommodation")) {
                    user.setStatus(Status.HOST);
                    user.setPhase(Phase.NAME);

                    this.buildButtonsService = new BuildButtonsService(new AddSkipButtonKeyboardRow());
                    return ibuildSendMessageService.createHTMLMessage(message.getChatId().toString(), "Type your name or SKIP if you want to set your default Telegram name", buildButtonsService.getMainMarkup());

                } else {
                    return ibuildSendMessageService.createHTMLMessage(message.getChatId().toString(), "You must press on a button!", buildButtonsService.getMainMarkup());
                }
        }
        return null;
    }
}