package com.nheatyon.searchoffersbot.config;

import com.nheatyon.searchoffersbot.InitializerClass;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FileCreator {

    public boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }

    @SneakyThrows
    public void createFromResources(String filePath, String fileName) {
        File file = new File(filePath + fileName);
        if (!isFileExists(file.getAbsolutePath())) {
            ClassLoader loader = InitializerClass.class.getClassLoader();
            try (InputStream inputStream = loader.getResourceAsStream(fileName)) {
                assert inputStream != null;
                String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                file.createNewFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + fileName))) {
                    writer.write(result);
                }
            }
        }
    }

    @SneakyThrows
    public File create(String filePath) {
        File file = new File(filePath);
        if (!isFileExists(file.getAbsolutePath())) {
            file.createNewFile();
        }
        return file;
    }

}
