package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.io.IOException;
import java.util.Properties;

//Loads properties file containing price in runtime.
public class TicketPriceLoader {
    private static final Properties properties = new Properties();

    static {
        try{
            properties.load(TicketPriceLoader.class.getClassLoader().getResourceAsStream("ticket-prices.properties"));

        }catch(IOException e){
            throw new RuntimeException("Ticket price file cannot be loaded.",e);
        }
    }

    public static int getPrice(String ticketType){
        String price = properties.getProperty(ticketType);
        return Integer.parseInt(price);
    }

    public void setMockProperties(Properties mockProperties){
        properties.clear();
        properties.putAll(mockProperties);
    }
}
