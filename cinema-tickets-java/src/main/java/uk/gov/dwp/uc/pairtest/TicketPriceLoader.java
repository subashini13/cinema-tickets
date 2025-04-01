package uk.gov.dwp.uc.pairtest;

import java.io.IOException;
import java.util.Properties;

//Loads properties file containing price in runtime.
public class TicketPriceLoader {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(TicketPriceLoader.class.getClassLoader().getResourceAsStream("ticket-prices.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ticket prices", e);
        }
    }
    public static int getPrice(String ticketType) {
        return Integer.parseInt(properties.getProperty(ticketType));
    }
}
