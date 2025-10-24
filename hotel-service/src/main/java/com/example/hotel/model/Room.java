package com.example.hotel.model;

import jakarta.persistence.*;

@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private int capacity;

    private long timesBooked;

    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public long getTimesBooked() { return timesBooked; }
    public void setTimesBooked(long timesBooked) { this.timesBooked = timesBooked; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }
}


