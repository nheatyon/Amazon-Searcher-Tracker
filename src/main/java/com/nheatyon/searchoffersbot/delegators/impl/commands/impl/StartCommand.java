package com.nheatyon.searchoffersbot.delegators.impl.commands.impl;

import com.nheatyon.searchoffersbot.annotations.BotCommand;
import com.nheatyon.searchoffersbot.delegators.impl.commands.GenericCommand;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@BotCommand(value = "start", syntax = "`/start`")
public final class StartCommand extends GenericCommand {

    public StartCommand(GenericCommandBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public void run() {
        getBot().sendWelcome(getUserId());
    }
}
