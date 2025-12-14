package org.example.foodanddrinkproject.event;

import org.example.foodanddrinkproject.dto.OrderDto;
import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final transient OrderDto orderDto;
    private final String oldStatus;
    private final String newStatus;

    public OrderStatusChangedEvent(Object source, OrderDto orderDto, String oldStatus, String newStatus) {
        super(source);
        this.orderDto = orderDto;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
