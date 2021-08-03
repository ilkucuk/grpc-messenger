package com.kucuk.client;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResultWriter implements Closeable {

    private PrintWriter printWriter;

    public ResultWriter(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        printWriter = new PrintWriter(fileWriter);
    }

    public void write(String line) {
        printWriter.write(line);
        printWriter.write(System.lineSeparator());
        printWriter.flush();
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }
}
