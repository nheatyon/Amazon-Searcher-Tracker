package com.nheatyon.searchoffersbot.delegators.impl.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Validator {

    public int extractDigits(String text) {
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

    public String getMatch(String regex, String text, int groupId) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group(groupId);
        }
        return "";
    }
}
