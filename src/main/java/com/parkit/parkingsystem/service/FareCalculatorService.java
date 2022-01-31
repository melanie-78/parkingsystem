package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

        long duration  = outHour.getTime() - inHour.getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(durationMinutes * (Fare.CAR_RATE_PER_HOUR)/60);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationMinutes * (Fare.BIKE_RATE_PER_HOUR)/60);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        if (durationMinutes <= 30)
        {
            ticket.setPrice(0);
        }

        if (ticket.isAlreadyCame()){
            BigDecimal result = BigDecimal.valueOf(ticket.getPrice()).multiply(BigDecimal.valueOf(0.95));
            ticket.setPrice(result.doubleValue());
        }

    }
}