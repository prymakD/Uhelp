package com.tehcman.input_final_destination.handlers;

import com.tehcman.cahce.Cache;
import com.tehcman.entities.Phase;
import com.tehcman.entities.Status;
import com.tehcman.entities.User;
import com.tehcman.input_final_destination.SendMessage_factories.ISendMessageFactory;
import com.tehcman.services.BuildButtonsService;
import com.tehcman.services.IBuildSendMessageService;
import com.tehcman.services.keyboards.AddSkipButtonKeyboardRow;
import com.tehcman.services.keyboards.AddStatusKeyboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class InitDataAndStatusHandler implements ISendMessageFactory {
    private final IBuildSendMessageService ibuildSendMessageService;
    private final Cache<User> userCache;
    private BuildButtonsService buildButtonsService;

    @Autowired
    public InitDataAndStatusHandler(IBuildSendMessageService ibuildSendMessageService, Cache<User> userCache) {
        this.ibuildSendMessageService = ibuildSendMessageService;
        this.userCache = userCache;
    }

    @Override
    public SendMessage createSendMessage(Message message) {
        User userFromCache = userCache.findBy(message.getChatId());
        if (userFromCache == null) {
            User newUser = generateDefaultUserInformationFromMessage(message);
            userCache.add(newUser);
            return ibuildSendMessageService.createHTMLMessage(message.getChatId().toString(), "How can we help you", buildButtonsService.getMainMarkup());

        } else if (userFromCache.getPhase() == Phase.NONE) {
            return new SendMessage(message.getChatId().toString(), "Hey. You are already in the system." + " Instead of duplicating data of yourself, do something useful in your life");
        } else {
            return registerRestUserData(userFromCache, message);
        }

    }

    private User generateDefaultUserInformationFromMessage(Message message) {
        User newUser = new User(message.getChatId(), message.getFrom().getUserName(), message.getFrom().getFirstName(), Phase.STATUS);
        this.buildButtonsService = new BuildButtonsService(new AddStatusKeyboard());
//        buildButtonsService.addingPhoneNumberButton(); //adding phone number button
        return newUser;
    }

    private SendMessage registerRestUserData(User user, Message message) {
        switch (user.getPhase()) {
            case STATUS:
                if (message.getText().equals("Searching Accommodation")) {
                    user.setStatus(Status.REFUGEE);
                    user.setPhase(Phase.NAME);

                    this.buildButtonsService = new BuildButtonsService(new AddSkipButtonKeyboardRow());
                    return ibuildSendMessageService.createHTMLMessage(message.getChatId().toString(), "Type your name or SKIP if you want to set your default Telegram name", buildButtonsService.getMainMarkup());
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
