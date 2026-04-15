package uk.gov.dwp.uc.pairtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {

    Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        LOGGER.info("Initiating ticket purchase for accountId: {}",accountId);
        validateAccountID(accountId);
        validateTicketType(ticketTypeRequests);

        int adultCount = 0, childCount = 0, infantCount = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case ADULT -> adultCount += request.getNoOfTickets();
                case CHILD -> childCount += request.getNoOfTickets();
                case INFANT -> infantCount += request.getNoOfTickets();
            }
        }

        LOGGER.debug("Ticket breakdown for accountId: {} adult- {}, child- {}, infant- {}",accountId,adultCount,
                childCount,infantCount);
        validateTicketConstraints(adultCount,childCount,infantCount);

        int totalPayment = calculateTotalPayment(adultCount,childCount,infantCount);
        processPayment(accountId,totalPayment);
        int totalSeat = adultCount + childCount;
        reserveSeats(accountId,totalSeat);
    }

    private int calculateTotalPayment(int adultCount, int childCount, int infantCount) {

        int adultPrice = TicketPriceLoader.getPrice("ADULT");
        int childPrice = TicketPriceLoader.getPrice("CHILD");
        int infantPrice = TicketPriceLoader.getPrice("INFANT");

        int totalPayment = (adultCount * adultPrice) + (childCount * childPrice) + (infantCount * infantPrice);
        return totalPayment;
    }

    private void validateAccountID(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID");
        }
    }
    private void validateTicketType(TicketTypeRequest... ticketTypeRequests){
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("No Ticket Requested.");
        }
        if(Arrays.stream(ticketTypeRequests).anyMatch(request -> request.getNoOfTickets() < 0)){
            throw new InvalidPurchaseException("Invalid Ticket Type or quantity.");
        }
    }

    private void validateTicketConstraints(int adultCount, int childCount, int infantCount){

        if ((adultCount + childCount + infantCount) > 25) {
            throw new InvalidPurchaseException("Ticket Limit Exceeded");
        }
        if ((adultCount == 0 && childCount > 0) || (adultCount == 0 && infantCount > 0)) {
            throw new InvalidPurchaseException("Child and Infant tickets require at least one Adult ticket");
        }

    }

    private void processPayment(Long accountId, int totalPayment){
        ticketPaymentService.makePayment(accountId,totalPayment);
    }
    private void reserveSeats(Long accountId, int totalSeats){
        seatReservationService.reserveSeat(accountId,totalSeats);
    }

}


