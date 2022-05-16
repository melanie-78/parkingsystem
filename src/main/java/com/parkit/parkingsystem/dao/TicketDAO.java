package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket){
        boolean result = false;
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            try{
                ps.setInt(1,ticket.getParkingSpot().getId());
                ps.setString(2, ticket.getVehicleRegNumber());
                ps.setDouble(3, ticket.getPrice());
                ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
                if (ticket.getOutTime() != null) {
                    ps.setTimestamp(5, (new Timestamp(ticket.getOutTime().getTime())) );
                } else {
                    ps.setTimestamp(5, null);
                }
                result = ps.execute();
            }finally {
                ps.close();
            }

        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
            result = false;
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return result;
    }

    public int getTicketNumberOccurrence(String vehicleRegNumber){
        Connection con = null;
        int result = 0;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.COUNT_TICKET_OCCURRENCE);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME
            ps.setString(1, vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            rs.next();
            result = rs.getInt("count(*)");
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
            return result;

        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return result;
        }
    }

    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        int ticketNumberOccurence = getTicketNumberOccurrence(vehicleRegNumber);
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));

                if(ticketNumberOccurence > 0) {
                    ticket.setAlreadyCame(true);

                }else {
                    ticket.setAlreadyCame(false);
                }
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            try {
                ps.setDouble(1, ticket.getPrice());
                ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
                ps.setInt(3, ticket.getId());
                ps.execute();
                return true;
            }finally {
                ps.close();            }
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }
}
