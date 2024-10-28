package org.psu.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.Session;

/**
 * Tests for {@link CreditReporter}
 */
public class CreditReporterTest {

	/**
	 * Tests onOpen
	 */
	@Test
	public void onOpen() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final int creditTotal = 100;

		final CreditReporter reporter = new CreditReporter();
		reporter.updateCreditTotal(creditTotal);

		reporter.onOpen(session1);

		verify(async1).sendText(Integer.toString(creditTotal));
	}

	/**
	 * Tests updateCreditTotal
	 */
	@Test
	public void updateCreditTotal() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final int creditTotal = 100;

		final CreditReporter reporter = new CreditReporter();

		reporter.onOpen(session1);
		reporter.onOpen(session2);

		// Update the total after we've established two connections
		reporter.updateCreditTotal(creditTotal);

		verify(async1).sendText(Integer.toString(creditTotal));
		verify(async2).sendText(Integer.toString(creditTotal));
	}

	/**
	 * Tests onClose
	 */
	@Test
	public void onClose() {

		final Async async1 = mock(Async.class);
		final Session session1 = mock(Session.class);
		when(session1.getAsyncRemote()).thenReturn(async1);

		final Async async2 = mock(Async.class);
		final Session session2 = mock(Session.class);
		when(session2.getAsyncRemote()).thenReturn(async2);

		final int creditTotal = 100;

		final CreditReporter reporter = new CreditReporter();

		reporter.onOpen(session1);
		reporter.onOpen(session2);

		reporter.onClose(session2);

		// Update the total, we only have session1 as an active session
		reporter.updateCreditTotal(creditTotal);

		verify(async1).sendText(Integer.toString(creditTotal));
		verify(async2, never()).sendText(Integer.toString(creditTotal));
	}

}
