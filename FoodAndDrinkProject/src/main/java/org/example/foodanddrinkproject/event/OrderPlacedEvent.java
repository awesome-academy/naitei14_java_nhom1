package org.example.foodanddrinkproject.event;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class OrderPlacedEvent extends ApplicationEvent {

    private final transient OrderDto orderDto;

    public OrderPlacedEvent(Object source, OrderDto orderDto) {
        super(source);
        this.orderDto = orderDto;
    }
}