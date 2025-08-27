package com.example.reservation; 
 
import java.time.LocalDateTime; 
 
public class Reservation { 
    private int id; 
    private String name; 
    private LocalDateTime reservationTime; 
 
    public Reservation(int id, String name, LocalDateTime reservationTime) { 
        this.id = id; 
        this.name = name; 
        this.reservationTime = reservationTime; 
    } 
 
    public int getId() { 
        return id; 
    } 
 
    public String getName() { 
        return name; 
    } 
 
    public LocalDateTime getReservationTime() { 
        return reservationTime; 
    } 

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", reservationTime=" + reservationTime +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation that = (Reservation) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}