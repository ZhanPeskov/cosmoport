package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/rest/ships", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ShipRest {
    // при первой выборке, когда нами количество кораблей на странице не определено
    // то по условию задачи устанавливаем это значение как 3
    private static long storedSize = 3L;

    // при первой выборке, когда по умолчанию тип сортировки установлен по номеру ID
    private static ShipOrder storedOrder = ShipOrder.ID;

    @Autowired
    private ShipRepository shipRepository;

    @GetMapping(path = {"", "/count"})
    public Object getShipsListAndCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        Stream<Ship> ships = StreamSupport.stream(shipRepository.findAll().spliterator(), false)
                .filter((ship) -> (name == null || ship.getName().contains(name)) &&
                        (planet == null || ship.getPlanet().contains(planet)) &&
                        (shipType == null || ship.getShipType() == shipType) &&
                        (after == null || !ship.getProdDate().before(new Date(after))) &&
                        (before == null || !ship.getProdDate().after(new Date(before))) &&
                        (isUsed == null || ship.getUsed() == isUsed) &&
                        (minSpeed == null || ship.getSpeed() >= minSpeed) &&
                        (maxSpeed == null || ship.getSpeed() <= maxSpeed) &&
                        (minCrewSize == null || ship.getCrewSize() >= minCrewSize) &&
                        (maxCrewSize == null || ship.getCrewSize() <= maxCrewSize) &&
                        (minRating == null || ship.getRating() >= minRating) &&
                        (maxRating == null || ship.getRating() <= maxRating)
                );

        if (request.getRequestURI().endsWith("/count")) {
            return ships.count();
        } else {
            // запоминаем последний указанный размер страницы, т.к. если этого не сделать то найдена ошибка в программе
            // например, если установить просмотр отличный от 3-х элементов и добавить новый корабль, то для новой выборки
            // pageNumber и pageSize передаются как null и по условиям задачи страницу мы должны будем отобразить с 3-мя элементами
            if (pageSize != null) {
                storedSize = pageSize;
            }

            // но НЕ ПРОХОДЯТ ТЕСТЫ, не проверяющие эту ошибку поэтому пишем такое условие
            // при исправлении ошибки это блок можно будет полностью удалить
            if (pageSize == null) {
                storedSize = 3L;
            }

            // запоминаем это значение именно через переменную т.к. выявлена ошибка в программе
            // например, если установить выборку не по номеру ID и добавить новый корабль, то
            // order передается как null и нам надо показывать сортировку по ID
            // хотя в элементе сортировки по прежнему отображается наш выбор
            // но закомментировано т.к. НЕ ПРОХОДЯТ ТЕСТЫ, не проверяющие эту ошибку
            // при исправлении ошибки убрать комментарии с блока IF
//            if (order != null) {
            storedOrder = order;
//            }

            return ships
                    .sorted((o1, o2) ->
                            storedOrder == ShipOrder.SPEED ? o1.getSpeed().compareTo(o2.getSpeed()) :
                                    storedOrder == ShipOrder.DATE ? o1.getProdDate().compareTo(o2.getProdDate()) :
                                            storedOrder == ShipOrder.RATING ? o1.getRating().compareTo(o2.getRating()) :
                                                    o1.getId().compareTo(o2.getId()))
                    .skip(pageNumber == null ? 0L : pageNumber * storedSize)
                    .limit(storedSize)
                    .collect(Collectors.toList());
        }
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<Ship> getShip(@PathVariable(value = "id") Long shipId) {
        if (shipId == null || shipId <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        final Ship ship = shipRepository.findById(shipId).orElse(null);

        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping()
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {

        if (ship == null || ship.checkName() <= 0 || ship.checkPlanet() <= 0 || ship.checkProdDate() <= 0 || ship.checkSpeed() <= 0 || ship.checkCrewSize() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        ship.setRoundedSpeedAndCalculatedRating();

        return new ResponseEntity<>(shipRepository.save(ship), HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping(value = "{id}")
    public ResponseEntity<Ship> updateShip(
            @PathVariable(value = "id") Long shipId,
            @RequestBody Ship shipNew
    ) {
        final ResponseEntity<Ship> entityShip = getShip(shipId);

        if (entityShip.getStatusCode() != HttpStatus.OK) {
            return entityShip;
        }

        final Ship shipOld = entityShip.getBody();

        successBlock:
        {
            switch (shipNew.checkName()) {
                case -1:
                    break successBlock;
                case 1:
                    shipOld.setName(shipNew.getName());
            }

            switch (shipNew.checkPlanet()) {
                case -1:
                    break successBlock;
                case 1:
                    shipOld.setPlanet(shipNew.getPlanet());
            }

            if (shipNew.getShipType() != null) {
                shipOld.setShipType(shipNew.getShipType());
            }

            switch (shipNew.checkProdDate()) {
                case -1:
                    break successBlock;
                case 1:
                    shipOld.setProdDate(shipNew.getProdDate());
            }

            if (shipNew.getUsed() != null) {
                shipOld.setUsed(shipNew.getUsed());
            }

            switch (shipNew.checkSpeed()) {
                case -1:
                    break successBlock;
                case 1:
                    shipOld.setSpeed(shipNew.getSpeed());
            }

            switch (shipNew.checkCrewSize()) {
                case -1:
                    break successBlock;
                case 1:
                    shipOld.setCrewSize(shipNew.getCrewSize());
            }

            return createShip(shipOld);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable(value = "id") Long shipId) {
        final ResponseEntity<Ship> entityShip = getShip(shipId);

        if (entityShip.getStatusCode() == HttpStatus.OK) {
            shipRepository.delete(entityShip.getBody());
        }

        return entityShip;
    }
}