package com.kucuk.server.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMessageRequest {

    @JsonProperty("RequestId")
    Long requestId;

    @JsonProperty("Title")
    String title;

    @JsonProperty("Content")
    String content;

    @JsonProperty("Author")
    String author;

    @JsonProperty("Time")
    Long time;

    @JsonProperty("SleepPeriod")
    Integer sleepPeriod;

    @JsonProperty("SampleDoubleField")
    Double sampleDoubleField;

    @JsonProperty("SampleIntegerField")
    Integer sampleIntegerField;

    @JsonProperty("SampleBooleanField")
    Boolean sampleBooleanField;
}
