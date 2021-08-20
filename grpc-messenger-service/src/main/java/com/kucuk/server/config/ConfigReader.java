package com.kucuk.server.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    public static MessageServiceConfig readServiceConfig(Path configFilePath) {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(configFilePath)) {
            return yaml.loadAs(in, MessageServiceConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
