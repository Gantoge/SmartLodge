package com.example.hotel.web;

import com.example.hotel.model.Room;
import com.example.hotel.model.RoomReservationLock;
import com.example.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class RoomController {
    private final HotelService hotelService;

    public RoomController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> get(@PathVariable Long id) {
        return hotelService.getRoom(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public Room create(@RequestBody Room r) { return hotelService.saveRoom(r); }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody Room r) {
        return hotelService.getRoom(id)
                .map(existing -> {
                    r.setId(id);
                    return ResponseEntity.ok(hotelService.saveRoom(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // Hold availability
    @PostMapping("/{id}/hold")
    public ResponseEntity<RoomReservationLock> hold(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        LocalDate start = LocalDate.parse(req.get("startDate"));
        LocalDate end = LocalDate.parse(req.get("endDate"));
        try {
            RoomReservationLock lock = hotelService.holdRoom(requestId, id, start, end);
            return ResponseEntity.ok(lock);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<RoomReservationLock> confirm(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        try {
            return ResponseEntity.ok(hotelService.confirmHold(requestId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<RoomReservationLock> release(@PathVariable Long id, @RequestBody Map<String, String> req) {
        String requestId = req.get("requestId");
        try {
            return ResponseEntity.ok(hotelService.releaseHold(requestId));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}


