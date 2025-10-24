package com.example.hotel.web;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.service.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class HotelController {
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<Hotel> list() { return hotelService.listHotels(); }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> get(@PathVariable Long id) {
        return hotelService.getHotel(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public Hotel create(@RequestBody Hotel h) { return hotelService.saveHotel(h); }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> update(@PathVariable Long id, @RequestBody Hotel h) {
        return hotelService.getHotel(id)
                .map(existing -> {
                    h.setId(id);
                    return ResponseEntity.ok(hotelService.saveHotel(h));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms")
    public List<Room> rooms() { return hotelService.listRooms(); }
}


