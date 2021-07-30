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

public class CreateMessageResponse {

    @JsonProperty("ResponseId")
    Long responseId;

    @JsonProperty("Hash")
    String hash;

    @JsonProperty("Time")
    Long time;

    @JsonProperty("SampleDoubleField")
    Double sampleDoubleField;

    @JsonProperty("SampleIntegerField")
    Integer sampleIntegerField;

    @JsonProperty("SampleBooleanField")
    Boolean sampleBooleanField;

    @JsonProperty("Messages")
    Message[] messages;
}
