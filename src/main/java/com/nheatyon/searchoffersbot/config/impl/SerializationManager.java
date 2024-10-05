package com.nheatyon.searchoffersbot.config.impl;

import com.nheatyon.searchoffersbot.config.ConfigurationManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializationManager implements ConfigurationManager<Object, String> {

    private static SerializationManager instance;

    public static SerializationManager getInstance() {
        if (instance == null) {
            instance = new SerializationManager();
        }
        return instance;
    }

    @Override
    @SneakyThrows
    public void write(Object object, Object... values) {
        String fileName = values[0] == null ? object.getClass().getSimpleName() : values[0].toString();
        try (FileOutputStream file = new FileOutputStream(fileName); ObjectOutputStream out = new ObjectOutputStream(file)) {
            out.writeObject(object);
        }
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T read(String fileName) {
        T object;
        try (FileInputStream file = new FileInputStream(fileName); ObjectInputStream in = new ObjectInputStream(file)) {
            object = (T) in.readObject();
        }
        return object;
    }
}
