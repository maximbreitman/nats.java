package io.nats.client.support;

import io.nats.client.Message;
import io.nats.client.Options;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * This is an internal class and is only public for access.
 */
public class NatsRequestCompletableFuture extends CompletableFuture<Message> {
    public enum CancelAction { CANCEL, REPORT, COMPLETE }
    private static final long DEFAULT_TIMEOUT = Options.DEFAULT_REQUEST_CLEANUP_INTERVAL.toMillis(); // currently 5 seconds

    private final CancelAction cancelAction;
    private final long timeOutAfter;
    private boolean wasCancelledClosing;
    private boolean wasCancelledTimedOut;
    private final boolean useTimeoutException;

    public NatsRequestCompletableFuture(CancelAction cancelAction, Duration timeout, boolean useTimeoutException) {
        this.cancelAction = cancelAction;
        timeOutAfter = System.currentTimeMillis() + 10 + (timeout == null ? DEFAULT_TIMEOUT : timeout.toMillis());
        // 10 extra millis allows for communication time, probably more than needed but...
        this.useTimeoutException = useTimeoutException;
    }

    public void cancelClosing() {
        wasCancelledClosing = true;
        completeExceptionally(new CancellationException("Future cancelled, connection closing."));
    }

    public void cancelTimedOut() {
        wasCancelledTimedOut = true;
        final String message = "Future cancelled, response not registered in time, likely due to server disconnect.";
        completeExceptionally(useTimeoutException ? new TimeoutException(message) : new CancellationException(message));
    }

    public CancelAction getCancelAction() {
        return cancelAction;
    }

    public boolean hasExceededTimeout() {
        return System.currentTimeMillis() > timeOutAfter;
    }

    public boolean wasCancelledClosing() {
        return wasCancelledClosing;
    }

    public boolean wasCancelledTimedOut() {
        return wasCancelledTimedOut;
    }
}
