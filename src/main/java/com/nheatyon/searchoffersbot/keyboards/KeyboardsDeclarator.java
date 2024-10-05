package com.nheatyon.searchoffersbot.keyboards;

import com.nheatyon.searchoffersbot.BotOperations;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.DatabaseOperator;
import com.nheatyon.searchoffersbot.closeable.database.impl.sqlite.operators.AdminOperator;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.config.PlaceholderParser;
import com.nheatyon.searchoffersbot.delegators.impl.commands.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public final class KeyboardsDeclarator {

    private final ConfigurationManager<?, String> config;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private InlineKeyboardMarkup retrieveMenu(JSONObject object, BiFunction<String, String, InlineKeyboardButton> bi) {
        return AsyncWrapper.supplyAsync(() -> {
            List<List<InlineKeyboardButton>> buttonsToApply = new ArrayList<>();
            Map<String, List<InlineKeyboardButton>> rowButtonPair = new HashMap<>();
            object.forEach((rowNumber, buttons) -> {
                JSONObject rowObject = (JSONObject) object.get(rowNumber);
                List<InlineKeyboardButton> singleRow = new ArrayList<>();
                rowObject.forEach((k, v) -> {
                    if (v instanceof JSONObject buttonObject) {
                        if (buttonObject.containsKey("callback")) {
                            singleRow.add(bi.apply(k.toString(), buttonObject.get("callback").toString()));
                        }
                        if (buttonObject.containsKey("url")) {
                            InlineKeyboardButton button = new InlineKeyboardButton();
                            button.setText(k.toString());
                            button.setUrl(buttonObject.get("url").toString());
                            singleRow.add(button);
                        }
                    } else {
                        singleRow.add(bi.apply(k.toString(), v.toString()));
                    }
                    rowButtonPair.put(rowNumber.toString(), singleRow);
                });
            });
            rowButtonPair.forEach((k, v) -> buttonsToApply.add(v));
            return new InlineKeyboardMarkup(buttonsToApply);
        }).get();
    }

    @SneakyThrows
    public InlineKeyboardMarkup get(KeyboardsType type) {
        return AsyncWrapper.supplyAsync(() -> {
            List<InlineKeyboardButton> buttonsToApply = new ArrayList<>();
            InlineKeyboardButton backButton = new InlineKeyboardButton(type.getValue());
            backButton.setCallbackData(type.getCallback());
            buttonsToApply.add(backButton);
            return new InlineKeyboardMarkup(Collections.singletonList(buttonsToApply));
        }).get();
    }

    private InlineKeyboardMarkup getMenu(KeyboardsType type) {
        String fieldName = type.getValue().toLowerCase();
        return retrieveMenu(config.read(fieldName), (text, callback) -> {
            InlineKeyboardButton kb = new InlineKeyboardButton(text);
            kb.setCallbackData(callback);
            return kb;
        });
    }

    public InlineKeyboardMarkup getParsableMenu(BotOperations bot, String userId) {
        DatabaseOperator adminOperator = bot.getOperators().get(AdminOperator.class);
        InlineKeyboardMarkup menu = getMenu(KeyboardsType.START_MENU);
        if (adminOperator.get(userId)) {
            InlineKeyboardMarkup countButton = get(KeyboardsType.COUNT_BUTTON);
            InlineKeyboardMarkup tracksButton = get(KeyboardsType.TRACKS_BUTTON);
            if (countButton == null || tracksButton == null) {
                return menu;
            }
            PlaceholderParser parser = new PlaceholderParser(config, bot.getOperators());
            countButton.getKeyboard().get(0).get(0).setText(parser.getCountableMessage());
            tracksButton.getKeyboard().get(0).get(0).setText(parser.getTrackedProductsMessage());
            // Add buttons to the menu
            menu.getKeyboard().add(List.of(
                    countButton.getKeyboard().get(0).get(0),
                    tracksButton.getKeyboard().get(0).get(0)
            ));
        }
        return menu;
    }

    public int getPage(CallbackQuery callback, boolean isNext) {
        Message msg = callback.getMessage();
        List<List<InlineKeyboardButton>> rows = msg.getReplyMarkup().getKeyboard();
        InlineKeyboardButton checkedButton = null;
        for (List<InlineKeyboardButton> row : rows) {
            for (InlineKeyboardButton button : row) {
                if (button.getText().contains(config.read("next_button")) && isNext) {
                    checkedButton = button;
                }
                if (button.getText().contains(config.read("back_button")) && !isNext) {
                    checkedButton = button;
                }
            }
        }
        Validator validator = new Validator();
        assert checkedButton != null;
        return validator.extractDigits(checkedButton.getText());
    }

    public InlineKeyboardMarkup getPaginationMenu(List<String> items, int currentPage, int rowLimit) {
        int totalItems = items.size();
        int itemsPerPage = ((Long) config.read("items_per_keyboard")).intValue();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        List<String> currentPageItems = new ArrayList<>(items.subList(startIndex, endIndex));
        List<List<InlineKeyboardButton>> buttonsToApply = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (String object : currentPageItems) {
            InlineKeyboardButton button = new InlineKeyboardButton(object);
            button.setCallbackData("get_category_offers_" + object);
            currentRow.add(button);
            if (currentRow.size() >= rowLimit) {
                buttonsToApply.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        if (!currentRow.isEmpty()) {
            buttonsToApply.add(currentRow);
        }
        List<InlineKeyboardButton> navigationButtons = new ArrayList<>();
        String backButtonText = String.format("#%s %s", currentPage - 1, config.read("back_button"));
        String nextButtonText = String.format("%s #%s", config.read("next_button"), currentPage + 1);
        if (currentPage > 1) {
            InlineKeyboardButton backButton = new InlineKeyboardButton();
            backButton.setText(backButtonText);
            backButton.setCallbackData("previous_page");
            navigationButtons.add(backButton);
        }
        if (currentPage < totalPages) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText(nextButtonText);
            nextButton.setCallbackData("next_page");
            navigationButtons.add(nextButton);
        }
        // Add navigation & homepage buttons
        List<InlineKeyboardButton> homepageButton = get(KeyboardsType.HOMEPAGE_BUTTON).getKeyboard().get(0);
        buttonsToApply.add(navigationButtons);
        buttonsToApply.add(homepageButton);
        return new InlineKeyboardMarkup(buttonsToApply);
    }
}
