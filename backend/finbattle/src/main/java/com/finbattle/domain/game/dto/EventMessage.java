package com.finbattle.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage<T> {

    private EventType event;
    private Long roomId;
    private T data;
}
