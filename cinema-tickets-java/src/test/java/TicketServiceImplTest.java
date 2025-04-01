import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TicketServiceImplTest {

    TicketPaymentService paymentService = mock(TicketPaymentService.class);
    SeatReservationService reservationService = mock(SeatReservationService.class);
    TicketServiceImpl ticketService = new TicketServiceImpl(paymentService, reservationService);

    @Test
    void shouldThrowExceptionForInvalidAccountId() {
        Long invalidAccountId = -1L;

        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(invalidAccountId, adultRequest);
        });

        assertEquals("Invalid account ID", exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }
    // Test for no tickets requested
    @Test
    void shouldThrowExceptionWhenNoTicketsRequested() {
        Long accountId = 123L;

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId);
        });

        assertEquals("No tickets requested", exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }
    // Test for invalid ticket quantity
    @Test
    void shouldThrowExceptionForInvalidTicketQuantity() {
        Long accountId = 123L;
        TicketTypeRequest invalidRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, invalidRequest);
        });

        assertEquals("Ticket quantity must be greater than zero", exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }

    // Test for exceeding maximum ticket limit
    @Test
    void shouldThrowExceptionForExceedingTicketLimit() {
        Long accountId = 123L;
        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, adultRequest);
        });

        assertEquals("Cannot purchase more than 25 tickets at a time", exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }


    // Test for child or infant tickets without adult tickets
    @Test
    void shouldThrowExceptionForChildOrInfantTicketsWithoutAdult() {
        Long accountId = 123L;
        TicketTypeRequest childRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, childRequest);
        });

        assertEquals("Child and Infant tickets require at least one Adult ticket", exception.getMessage());
        verifyNoInteractions(paymentService, reservationService);
    }



@Test
    public void shouldCalculateTotalPaymentCorrectly() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        ticketService.purchaseTickets(1L, adult, child);
        verify(paymentService).makePayment(1L, 95); // £50 for adults + £45 for children
    }


    @Test
    public void shouldReserveCorrectNumberOfSeats() {
        TicketTypeRequest adult = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest child = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infant = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        ticketService.purchaseTickets(1L, adult, child, infant);
        verify(reservationService).reserveSeat(1L, 5); // Adults + Children = 5 seats
    }
}
