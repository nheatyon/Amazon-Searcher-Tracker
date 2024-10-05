package com.nheatyon.searchoffersbot.keyboards;

import com.nheatyon.searchoffersbot.config.impl.JsonManager;
import lombok.Getter;

@Getter
public enum KeyboardsType {

    START_MENU("start_menu", null),
    COUNT_BUTTON("Total Users: ", "none"),
    TRACKS_BUTTON("Total Tracks: ", "none"),
    HOMEPAGE_BUTTON(JsonManager.getInstance().read("homepage_button"), "empty"),
    TERMINATE_BUTTON(JsonManager.getInstance().read("terminate_button"), "terminate_operations"),
    BACK_BUTTON(JsonManager.getInstance().read("back_button"), "empty");

    private final String value;
    private final String callback;

    KeyboardsType(String value, String callback) {
        this.value = value;
        this.callback = callback;
    }
}
