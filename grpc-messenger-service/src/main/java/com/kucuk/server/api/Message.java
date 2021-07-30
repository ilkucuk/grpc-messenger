package com.kucuk.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @JsonProperty("MessageId")
    Long id;

    @JsonProperty("Content")
    String content;

    @JsonProperty("Time")
    Long time;
}
