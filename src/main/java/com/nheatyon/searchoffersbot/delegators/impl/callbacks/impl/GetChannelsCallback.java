package com.nheatyon.searchoffersbot.delegators.impl.callbacks.impl;

import com.nheatyon.searchoffersbot.annotations.BotCallback;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.delegators.impl.callbacks.GenericCallback;
import com.nheatyon.searchoffersbot.keyboards.KeyboardsType;
import lombok.experimental.SuperBuilder;
import org.json.simple.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.Map;

@SuperBuilder
@BotCallback(value = "get_channels")
public final class GetChannelsCallback extends GenericCallback {

    public GetChannelsCallback(GenericCallbackBuilder<?, ?> builder) {
        super(builder);
    }

    private Map<Integer, JSONObject> getMappedChannels(ConfigurationManager<?, String> config) {
        Map<Integer, JSONObject> map = new LinkedHashMap<>();
        JSONObject channels = config.read("official_channels");
        for (int i = 0; i < channels.size(); i++) {
            map.put(i, (JSONObject) channels.get(String.valueOf(i + 1)));
        }
        return map;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        ConfigurationManager<?, String> config = getBot().getConfig();
        String format = config.read("official_channels_format");
        getMappedChannels(config).forEach((index, channel) -> {
            String parsedFormat = format
                    .replace("%channel%", String.format("<b>%s</b>", channel.get("channel_name")))
                    .replace("%link%", channel.get("link").toString());
            sb.append(parsedFormat).append("\n\n");
        });
        InlineKeyboardMarkup backButton = getBot().getKeyboards().get(KeyboardsType.BACK_BUTTON);
        getBot().editMessage(getCallback(), sb.toString(), backButton);
    }
}
