package com.space.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String planet;
    @Enumerated(EnumType.STRING)
    private ShipType shipType;
    private Date prodDate;
    private Boolean isUsed;
    private Double speed;
    private Integer crewSize;
    private Double rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlanet() {
        return planet;
    }

    public void setPlanet(String planet) {
        this.planet = planet;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setShipType(ShipType shipType) {
        this.shipType = shipType;
    }

    public Date getProdDate() {
        return prodDate;
    }

    public void setProdDate(Date prodDate) {
        this.prodDate = prodDate;
    }

    public Boolean getUsed() {
        return isUsed;
    }

    public void setUsed(Boolean used) {
        isUsed = used;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Integer getCrewSize() {
        return crewSize;
    }

    public void setCrewSize(Integer crewSize) {
        this.crewSize = crewSize;
    }

    public Double getRating() {
        return rating;
    }

    public void setRoundedSpeedAndCalculatedRating() {
        speed = mathRound(speed);
        rating = mathRound(80 * speed * (isUsed ? 0.5 : 1) / (3019 - getProdDateYear() + 1));
    }

    public int getProdDateYear() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(prodDate);

        return calendar.get(Calendar.YEAR);
    }

    public int checkName() {
        return name == null ? 0 : name.isEmpty() || name.length() > 50 ? -1 : 1;
    }

    public int checkPlanet() {
        return planet == null ? 0 : planet.isEmpty() || planet.length() > 50 ? -1 : 1;
    }

    public int checkProdDate() {
        if (prodDate == null) {
            return 0;
        }

        final int year = getProdDateYear();

        return year >= 2800 && year <= 3019 ? 1 : -1;
    }

    public int checkSpeed() {
        return speed == null ? 0 : speed >= 0.01 && speed <= 0.99 ? 1 : -1;
    }

    public int checkCrewSize() {
        return crewSize == null ? 0 : crewSize >= 1 && crewSize <= 9999 ? 1 : -1;
    }

    private double mathRound(double value) {
        return Math.round(value * 100) / 100D;
    }
}