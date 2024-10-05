package com.nheatyon.searchoffersbot.config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nheatyon.searchoffersbot.async.AsyncWrapper;
import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import com.nheatyon.searchoffersbot.config.FileCreator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonManager implements ConfigurationManager<JSONObject, String> {

    private String fileName;
    private JSONObject object;
    private static JsonManager instance;

    public static JsonManager getInstance() {
        if (instance == null) {
            instance = new JsonManager();
            String jsonName = "settings.json";
            FileCreator creator = new FileCreator();
            creator.createFromResources("", jsonName);
            instance.fileName = jsonName;
            // Get the JSON Object
            try (Reader reader = new FileReader(instance.fileName)) {
                instance.object = (JSONObject) new JSONParser().parse(reader);
            } catch (IOException | ParseException e) {
                Logger.getRootLogger().error(e);
            }
        }
        return instance;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void write(JSONObject item, Object... values) {
        AsyncWrapper.runAsync(() -> {
            String fileName = values[0] == null ? this.fileName : values[0].toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
                JsonElement je = new JsonParser().parse(item.toJSONString());
                writer.write(gson.toJson(je));
            } catch (IOException e) {
                Logger.getRootLogger().error(e);
            }
        });
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T read(String key) {
        return (T) AsyncWrapper.supplyAsync(() -> object.get(key)).get();
    }

    @SuppressWarnings("unchecked")
    public <T> void replace(String key, T value) {
        object.replace(key, value);
        write(object);
    }
}
