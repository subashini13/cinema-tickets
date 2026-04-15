import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class TicketServiceImplTest{

    private static Logger LOGGER = LoggerFactory.getLogger(TicketServiceImplTest.class);

    TicketPaymentService ticketPaymentService = mock(TicketPaymentService.class);
    SeatReservationService seatReservationService = mock(SeatReservationService.class);
    TicketServiceImpl ticketservice = new TicketServiceImpl(ticketPaymentService,seatReservationService);

    @Test
    void shouldThrowExceptionForInvalidAccountID(){
        LOGGER.info("Test: Invalid account ID");
        Long invalidAccountID = -1L;

        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,2);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
                    ticketservice.purchaseTickets(invalidAccountID,adult);
                });
        assertEquals("Invalid account ID", exception.getMessage());
        verifyNoInteractions(ticketPaymentService,seatReservationService);
    }

    @Test
    void shouldThrowExceptionForInvalidTicketQuantity(){
        LOGGER.info("Test : Invalid Ticket request ");

    InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class ,() -> {
            new TicketTypeRequest(TicketTypeRequest.Type.ADULT,0);
    });
    assertEquals("Invalid Ticket Type or quantity.", exception.getMessage());
    verifyNoInteractions(ticketPaymentService,seatReservationService);

    }
    @Test
    void shouldThrowExceptionWhenNoTicketRequested(){
        LOGGER.info("Test: No Ticket Requested");

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () ->{
            ticketservice.purchaseTickets(1L);
        });

        assertEquals("No Ticket Requested.", exception.getMessage());
        verifyNoInteractions(ticketPaymentService,seatReservationService);

    }
    @Test
    void shouldThrowExceptionWhenTicketLimitExceeds(){

        LOGGER.info("Test: Exceeding Ticket Limit");

        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 10);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 6);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketservice.purchaseTickets(1L, adult,child, infant);
        });

        assertEquals("Ticket Limit Exceeded" , exception.getMessage());
        verifyNoInteractions(ticketPaymentService,seatReservationService);
    }

    @Test
    void shouldThrowExceptionForChildOrInfantWithoutAdult(){

        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD , 1);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 1);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, ()->{
            ticketservice.purchaseTickets(2L, child,infant);
        });

        assertEquals("Child and Infant tickets require at least one Adult ticket", exception.getMessage());
        verifyNoInteractions(ticketPaymentService,seatReservationService);
    }
   /* @Test
    void shouldPassWithMaximumTicketLimit(){
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,15);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,5);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,5);

        ticketservice.purchaseTickets(2L, adult,child,infant);
        verify(ticketPaymentService).makePayment();
    }*/

    @Test
    void shouldCalculatePaymentCorrectly(){
        LOGGER.info("Test: Total payment calculation");
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,2);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,1);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,1);

        ticketservice.purchaseTickets(2L, adult,infant,child);
        verify(ticketPaymentService).makePayment(2L,65);
    }

    @Test
    void shouldCalculateSeatsCorrectly(){
        LOGGER.info("Test: Total Seat Calculation");
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,2);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,1);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,1);

        ticketservice.purchaseTickets(2L,adult,child,infant);
        verify(seatReservationService).reserveSeat(2L,3);
    }

    @Test
    void shouldPassForSingleAdultTicket(){
        LOGGER.info("Test: Single Adult Ticket");
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,1);

        ticketservice.purchaseTickets(2L, adult);
        verify(ticketPaymentService).makePayment(2L,25);
        verify(seatReservationService).reserveSeat(2L,1);
    }

    @Test
    void shouldPassForMaximumTicketLimit(){
        LOGGER.info("Test: Maximum Ticket Limit");
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT,15);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD,5);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT,5);

        ticketservice.purchaseTickets(2L, adult,child,infant);
        verify(ticketPaymentService).makePayment(2L,450);
        verify(seatReservationService).reserveSeat(2L, 20);
    }




}
