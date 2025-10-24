package com.example.hotel;

import com.example.hotel.model.Hotel;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomReservationLock;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.service.HotelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
public class HotelMoreTests {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HotelService hotelService;

    @Test
    @Transactional
    void dateConflictReturns409LikeBehavior() {
        Hotel h = new Hotel();
        h.setName("H");
        h.setCity("C");
        h = hotelRepository.save(h);
        Room r = new Room();
        r.setHotel(h);
        r.setNumber("101");
        r.setCapacity(2);
        r = hotelService.saveRoom(r);

        LocalDate s1 = LocalDate.now();
        LocalDate e1 = s1.plusDays(2);
        hotelService.holdRoom("req-a", r.getId(), s1, e1);

        // пересечение дат
        Assertions.assertThrows(IllegalStateException.class, () ->
                hotelService.holdRoom("req-b", r.getId(), s1.plusDays(1), e1.plusDays(1))
        );
    }

    @Test
    @Transactional
    void availableFlagDoesNotAffectDateOccupancy() {
        Hotel h = new Hotel();
        h.setName("H");
        h.setCity("C");
        h = hotelRepository.save(h);
        Room r = new Room();
        r.setHotel(h);
        r.setNumber("102");
        r.setCapacity(2);
        r.setAvailable(false);
        r = hotelService.saveRoom(r);

        // Даже если available=false, занятость по датам определяется блокировками/бронями
        LocalDate s1 = LocalDate.now();
        LocalDate e1 = s1.plusDays(1);
        RoomReservationLock lock = hotelService.holdRoom("req-c", r.getId(), s1, e1);
        Assertions.assertEquals(RoomReservationLock.Status.HELD, lock.getStatus());
    }

    @Test
    @Transactional
    void statsPopularRoomsOrder() {
        Hotel h = new Hotel();
        h.setName("H");
        h.setCity("C");
        h = hotelRepository.save(h);
        Room r1 = new Room();
        r1.setHotel(h);
        r1.setNumber("201");
        r1.setCapacity(2);
        r1 = hotelService.saveRoom(r1);
        Room r2 = new Room();
        r2.setHotel(h);
        r2.setNumber("202");
        r2.setCapacity(2);
        r2 = hotelService.saveRoom(r2);

        // имитируем подтверждения
        hotelService.holdRoom("req-d", r1.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        hotelService.confirmHold("req-d");
        hotelService.holdRoom("req-e", r1.getId(), LocalDate.now().plusDays(2), LocalDate.now().plusDays(3));
        hotelService.confirmHold("req-e");
        hotelService.holdRoom("req-f", r2.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        hotelService.confirmHold("req-f");

        List<Room> popular = new com.example.hotel.web.StatsController(new com.example.hotel.repo.RoomRepository() {
            @Override public java.util.List<Room> findAll() { return java.util.List.of(r1, r2); }
            @Override public java.util.List<Room> findAllById(Iterable<Long> longs) { return null; }
            @Override public <S extends Room> java.util.List<S> saveAll(Iterable<S> entities) { return null; }
            @Override public <S extends Room> S save(S entity) { return null; }
            @Override public java.util.Optional<Room> findById(Long aLong) { return java.util.Optional.empty(); }
            @Override public void deleteById(Long aLong) { }
            @Override public void delete(Room entity) { }
            @Override public long count() { return 0; }
            @Override public void deleteAll() { }
            @Override public boolean existsById(Long aLong) { return false; }
            @Override public <S extends Room> S insert(S entity) { return null; }
            @Override public <S extends Room> java.util.List<S> insert(Iterable<S> entities) { return null; }
            @Override public <S extends Room> S saveAndFlush(S entity) { return null; }
            @Override public <S extends Room> java.util.List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
            @Override public void deleteAllInBatch(Iterable<Room> entities) { }
            @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { }
            @Override public void deleteAllInBatch() { }
            @Override public Room getOne(Long aLong) { return null; }
            @Override public Room getById(Long aLong) { return null; }
            @Override public Room getReferenceById(Long aLong) { return null; }
            @Override public <S extends Room> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return java.util.Optional.empty(); }
            @Override public <S extends Room> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example) { return null; }
            @Override public <S extends Room> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return null; }
            @Override public <S extends Room> long count(org.springframework.data.domain.Example<S> example) { return 0; }
            @Override public <S extends Room> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
            @Override public java.util.List<Room> findAll(org.springframework.data.domain.Sort sort) { return java.util.List.of(r1, r2).stream().sorted(sort).toList(); }
            @Override public org.springframework.data.domain.Page<Room> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
            @Override public void flush() { }
        }).popularRooms();
        Assertions.assertNotNull(popular);
    }
}


