package com.app.ecommerce.shared.exception;

import com.app.ecommerce.shared.enums.Status;

public class InvalidStateTransitionException extends RuntimeException {

    private final Status currentStatus;
    private final Status requestedStatus;

    public InvalidStateTransitionException(Status currentStatus, Status requestedStatus) {
        super("Cannot transition from " + currentStatus + " to " + requestedStatus + 
              ". Allowed transitions from " + currentStatus + ": " + 
              (currentStatus.getAllowedTransitions().isEmpty() ? 
               "(none — terminal state)" : currentStatus.getAllowedTransitions()));
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public Status getRequestedStatus() {
        return requestedStatus;
    }
}
