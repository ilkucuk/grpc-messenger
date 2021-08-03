package com.kucuk.client.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    public static ClientConfig readServiceConfig(Path configFilePath) {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(configFilePath)) {
            ClientConfig config = yaml.loadAs(in, ClientConfig.class);
            System.out.println(config.toString());
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
