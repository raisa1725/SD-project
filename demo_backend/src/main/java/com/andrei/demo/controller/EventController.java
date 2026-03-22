package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Event;
import com.andrei.demo.model.EventCreateDTO;
import com.andrei.demo.service.EventService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class EventController {
    private final EventService eventService;

    @GetMapping("/event")
    public List<Event> getEvents() {
        return eventService.getEvents();
    }

    @GetMapping("/event/{uuid}")
    public Event getEventById(@PathVariable UUID uuid) {
        return eventService.getEventById(uuid);
    }

    @PostMapping("/event")
    public Event addEvent(
            @Valid @RequestBody EventCreateDTO eventDTO
    ) {
        return eventService.addEvent(eventDTO);
    }

    @PutMapping("/event/{uuid}")
    public Event updateEvent(@PathVariable UUID uuid, @RequestBody Event event) {
        return eventService.updateEvent(uuid, event);
    }

    @DeleteMapping("/event/{uuid}")
    public void deleteEvent(@PathVariable UUID uuid) {
        eventService.deleteEvent(uuid);
    }
}