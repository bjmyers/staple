package org.psu.shiporchestrator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.shippurchase.ShipPurchaseJob;
import org.psu.shippurchase.ShipPurchaseManager;
import org.psu.trademanager.TradeShipManager;
import org.psu.trademanager.dto.TradeShipJob;

/**
 * Tests for {@link ShipJobQueue}
 */
public class ShipJobQueueTest {

	/**
	 * Tests the case where the queue is started but it is empty
	 */
	@Test
	public void emptyQueue() {

		final ShipJobQueue queue = new ShipJobQueue(null, null, null);

		assertThrows(IllegalStateException.class, () -> queue.beginJobQueue());
	}

	/**
	 * Tests the case where the next job is ready to perform
	 */
	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	public void beginJobQueueReady() {

		final TradeShipManager tradeShipManager = mock();
		final MiningShipManager miningShipManager = mock();
		final ShipPurchaseManager shipPurchaseManager = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager);

		final TradeShipJob tradeJob = mock();
		when(tradeJob.getNextAction()).thenReturn(Instant.now());
		final MiningShipJob miningJob = mock();
		when(miningJob.getNextAction()).thenReturn(Instant.now());
		final ShipPurchaseJob purchaseJob = mock();
		when(purchaseJob.getNextAction()).thenReturn(Instant.now());
		final ShipJob otherShipJob = mock();
		when(otherShipJob.getNextAction()).thenReturn(Instant.now());

		final Instant oneSecondFromNow = Instant.now().plus(Duration.ofSeconds(1));
		final TradeShipJob laterTradeJob = mock();
		when(laterTradeJob.getNextAction()).thenReturn(oneSecondFromNow);
		final MiningShipJob laterMiningJob = mock();
		when(laterMiningJob.getNextAction()).thenReturn(oneSecondFromNow);
		final ShipPurchaseJob laterPurchaseJob = mock();
		when(laterPurchaseJob.getNextAction()).thenReturn(oneSecondFromNow);

		// IMPORTANT: Throw an exception on the second iteration to stop us from running
		// forever
		when(tradeShipManager.manageTradeShip(tradeJob)).thenReturn(laterTradeJob).thenThrow(RuntimeException.class);
		when(miningShipManager.manageMiningShip(miningJob)).thenReturn(laterMiningJob)
				.thenThrow(RuntimeException.class);
		when(shipPurchaseManager.manageShipPurchase(purchaseJob)).thenReturn(laterPurchaseJob)
				.thenThrow(RuntimeException.class);

		queue.establishJobs(List.of(tradeJob, miningJob, purchaseJob, otherShipJob));
		// By asserting it throws, we ensure that it was called twice
		assertThrows(RuntimeException.class, () -> queue.beginJobQueue());
	}

	/**
	 * Tests the case where the queue needs to wait to perform the next job
	 */
	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	public void beginJobQueueWait() {

		final TradeShipManager tradeShipManager = mock();
		final MiningShipManager miningShipManager = mock();
		final ShipPurchaseManager shipPurchaseManager = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager);

		final TradeShipJob job = mock(TradeShipJob.class);
		// Give it enough time that it will have to wait
		when(job.getNextAction()).thenReturn(Instant.now().plus(Duration.ofSeconds(2)));

		// IMPORTANT: Throw an exception on the second iteration to stop us from running forever
		when(tradeShipManager.manageTradeShip(job)).thenReturn(job).thenThrow(RuntimeException.class);

		queue.establishJobs(List.of(job));
		// By asserting it throws, we ensure that it was called twice
		assertThrows(RuntimeException.class, () -> queue.beginJobQueue());
	}

}
