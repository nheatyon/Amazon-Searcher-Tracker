package com.nheatyon.searchoffersbot.config;

public interface ConfigurationManager<W, R> {

    void write(W data, Object... values);
    <T> T read(R data);
}
