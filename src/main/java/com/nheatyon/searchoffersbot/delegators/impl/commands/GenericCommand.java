package com.nheatyon.searchoffersbot.delegators.impl.commands;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.annotations.BotCommand;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
@SuperBuilder
public abstract class GenericCommand {

    private final String userId;
    private final String chatId;
    private final String[] args;
    private final BotOperations bot;
    private final BotCommand annotation;
    private final Update update;

    public GenericCommand(GenericCommandBuilder<?, ?> builder) {
        this.userId = builder.userId;
        this.chatId = builder.chatId;
        this.args = builder.args;
        this.bot = builder.bot;
        this.annotation = builder.annotation;
        this.update = builder.update;
    }

    public abstract void run();
}
