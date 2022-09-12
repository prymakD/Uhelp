package com.tehcman.input_final_destination.handlers;

import com.tehcman.cahce.UserCache;
import com.tehcman.entities.Status;
import com.tehcman.entities.User;
import com.tehcman.input_final_destination.SendMessage_factories.Text1SendMessageFactory;
import com.tehcman.input_final_destination.SendMessage_factories.ISendMessageAbstractFactory;
import com.tehcman.input_final_destination.SendMessage_factories.Text2SendMessageAbstractFactory;
import com.tehcman.printers.PrintHost;
import com.tehcman.sendmessage.MessageSender;
import com.tehcman.services.IBuildSendMessageService;
import com.tehcman.services.ParsingJSONtoListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TextHandler implements IHandler<Message> {
    private final MessageSender messageSender;
    private final PrintHost printHost;
    private final Text1SendMessageFactory text1SendMessageFactory;
    private final ISendMessageAbstractFactory create2SendMessagesFactory;
    private final UserCache userCache;
    private final IBuildSendMessageService iBuildSendMessageService;



    @Autowired
    public TextHandler(@Lazy MessageSender messageSender, PrintHost printHost, Text1SendMessageFactory text1SendMessageFactory, Text2SendMessageAbstractFactory create2SendMessagesFactory, UserCache userCache, IBuildSendMessageService iBuildSendMessageService) {
        this.messageSender = messageSender;
        this.printHost = printHost;
        this.text1SendMessageFactory = text1SendMessageFactory;
        this.create2SendMessagesFactory = create2SendMessagesFactory;
        this.userCache = userCache;
        this.iBuildSendMessageService = iBuildSendMessageService;
    }

    @Override
    public void handle(Message message) {
        //handling possible prior message to a user
        if (message.getText().equals("List of news channels about Ukraine (ENG)")) {
            SendMessage msg1 = create2SendMessagesFactory.create1stSendMessage(message);
            SendMessage msg2 = create2SendMessagesFactory.create2ndSendMessage(message);

            messageSender.messageSend(msg1);
            messageSender.messageSend(msg2);

            //TODO behavior: passing the list based on the filter to the inline button message
        } else if ((message.getText().equals("Show me shelter seeking people"))) {
            List<User> list = userCache.getAll().stream()
                    .filter(x -> x.getStatus().equals(Status.REFUGEE))
                    .collect(Collectors.toList());

            ParsingJSONtoListService parsingJSONtoListService = new ParsingJSONtoListService();
            list.addAll(parsingJSONtoListService.parse());
            for (User user : list) {
                System.out.println(user.toString());
            }
            SendMessage sendMessage = iBuildSendMessageService.createHTMLMessage(String.valueOf(message.getChatId()), list.toString(), new ReplyKeyboardRemove(true));
            messageSender.messageSend(sendMessage);

        } else if ((message.getText().equals("Show me shelter providing people"))) {
            printHost.printHostRandom(message);

/*
            List<User> list = userCache.getAll();
            for (User user : list) {
                System.out.println(user.toString());
            }
*/
//            SendMessage sendMessage = iBuildSendMessageService.createHTMLMessage(String.valueOf(message.getChatId()), list.toString(), new ReplyKeyboardRemove(true));
//            messageSender.messageSend(sendMessage);
        } else {
            SendMessage newMessageToUser = text1SendMessageFactory.createSendMessage(message);

            messageSender.messageSend(newMessageToUser);
        }
    }
}