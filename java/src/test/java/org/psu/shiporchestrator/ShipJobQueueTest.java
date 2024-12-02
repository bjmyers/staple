package org.psu.shiporchestrator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.psu.init.ShipJobCreator;
import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.shippurchase.ShipPurchaseJob;
import org.psu.shippurchase.ShipPurchaseManager;
import org.psu.shippurchase.ShipPurchaseManager.ShipPurchaseManagerResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipType;
import org.psu.trademanager.TradeShipManager;
import org.psu.trademanager.dto.TradeShipJob;
import org.psu.websocket.WebsocketReporter;

/**
 * Tests for {@link ShipJobQueue}
 */
public class ShipJobQueueTest {

	/**
	 * Tests the case where the queue is started but it is empty
	 */
	@Test
	public void emptyQueue() {

		final ShipJobQueue queue = new ShipJobQueue(null, null, null, null, null);

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
		final ShipJobCreator shipJobCreator = mock();
		final WebsocketReporter websocketReporter = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager,
				shipJobCreator, websocketReporter);

		final TradeShipJob tradeJob = mock();
		when(tradeJob.getNextAction()).thenReturn(Instant.now());
		final MiningShipJob miningJob = mock();
		when(miningJob.getNextAction()).thenReturn(Instant.now());
		final ShipJob otherShipJob = mock();
		when(otherShipJob.getNextAction()).thenReturn(Instant.now());

		final Instant oneSecondFromNow = Instant.now().plus(Duration.ofSeconds(1));
		final TradeShipJob laterTradeJob = mock();
		when(laterTradeJob.getNextAction()).thenReturn(oneSecondFromNow);
		final MiningShipJob laterMiningJob = mock();
		when(laterMiningJob.getNextAction()).thenReturn(oneSecondFromNow);

		// IMPORTANT: Throw an exception on the second iteration to stop us from running
		// forever
		when(tradeShipManager.manageTradeShip(tradeJob)).thenReturn(laterTradeJob).thenThrow(RuntimeException.class);
		when(miningShipManager.manageMiningShip(miningJob)).thenReturn(laterMiningJob)
				.thenThrow(RuntimeException.class);

		queue.establishJobs(List.of(tradeJob, miningJob, otherShipJob));
		// By asserting it throws, we ensure that it was called twice
		assertThrows(RuntimeException.class, () -> queue.beginJobQueue());
	}

	/**
	 * Tests the case where a purchase job has not yet been completed
	 */
	@Test
	public void beginJobQueuePurchaseNotDone() {

		final TradeShipManager tradeShipManager = mock();
		final MiningShipManager miningShipManager = mock();
		final ShipPurchaseManager shipPurchaseManager = mock();
		final ShipJobCreator shipJobCreator = mock();
		final WebsocketReporter websocketReporter = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager,
				shipJobCreator, websocketReporter);

		final ShipPurchaseJob purchaseJob = mock();
		when(purchaseJob.getNextAction()).thenReturn(Instant.now());

		final ShipPurchaseJob nextJob = mock();
		when(nextJob.getNextAction()).thenReturn(Instant.now());

		// Job is not done
		when(shipPurchaseManager.manageShipPurchase(purchaseJob)).thenReturn(new ShipPurchaseManagerResponse(nextJob, null));
		// Ensures that we don't go on forever
		when(shipPurchaseManager.manageShipPurchase(nextJob)).thenThrow(IllegalArgumentException.class);

		queue.establishJobs(List.of(purchaseJob));
		assertThrows(IllegalArgumentException.class, () -> queue.beginJobQueue());
	}

	/**
	 * Tests the case where a purchase job has been completed
	 */
	@Test
	public void beginJobQueuePurchaseDone() {

		final TradeShipManager tradeShipManager = mock();
		final MiningShipManager miningShipManager = mock();
		final ShipPurchaseManager shipPurchaseManager = mock();
		final ShipJobCreator shipJobCreator = mock();
		final WebsocketReporter websocketReporter = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager,
				shipJobCreator, websocketReporter);

		final Ship ship = mock();

		// The original purchase job
		final ShipPurchaseJob purchaseJob = mock();
		when(purchaseJob.getShip()).thenReturn(ship);
		when(purchaseJob.getNextAction()).thenReturn(Instant.now());

		// These are the jobs for the ship which did the purchasing
		final TradeShipJob nextJob = mock();
		when(nextJob.getNextAction()).thenReturn(Instant.now().minus(Duration.ofSeconds(1)));
		final TradeShipJob laterJob = mock();
		when(laterJob.getNextAction()).thenReturn(Instant.now().plus(Duration.ofSeconds(1)));
		when(shipJobCreator.createShipJob(ship)).thenReturn(nextJob);

		// This job's next action is after that of nextJob, so it will be processed second
		final Ship newShip = mock();
		final MiningShipJob jobForNewShip = mock();
		when(jobForNewShip.getNextAction()).thenReturn(Instant.now());
		when(jobForNewShip.getShip()).thenReturn(newShip);
		when(shipJobCreator.createShipJob(newShip)).thenReturn(jobForNewShip);

		when(shipPurchaseManager.manageShipPurchase(purchaseJob)).thenReturn(new ShipPurchaseManagerResponse(null, newShip));

		when(tradeShipManager.manageTradeShip(nextJob)).thenReturn(laterJob);
		when(miningShipManager.manageMiningShip(jobForNewShip)).thenThrow(IllegalArgumentException.class);

		queue.establishJobs(List.of(purchaseJob));
		assertThrows(IllegalArgumentException.class, () -> queue.beginJobQueue());
		verify(websocketReporter).addShip(newShip);
	}

	/**
	 * Tests the case where a purchase job must be newly created
	 */
	@Test
	public void beginJobQueueStartPurchase() {

		final TradeShipManager tradeShipManager = mock();
		final MiningShipManager miningShipManager = mock();
		final ShipPurchaseManager shipPurchaseManager = mock();
		final ShipJobCreator shipJobCreator = mock();
		final WebsocketReporter websocketReporter = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager,
				shipJobCreator, websocketReporter);

		final Ship ship = mock();

		// The original job
		final TradeShipJob tradeJob = mock();
		when(tradeJob.getShip()).thenReturn(ship);
		when(tradeJob.getNextAction()).thenReturn(Instant.now());

		// Time for a new job
		when(tradeShipManager.manageTradeShip(tradeJob)).thenReturn(null);

		// Ensures that the job queue will produce a new purchase job
		queue.setShipTypeToBuy(ShipType.SHIP_EXPLORER);

		final ShipPurchaseJob shipPurchaseJob = mock();
		when(shipPurchaseJob.getNextAction()).thenReturn(Instant.now());
		when(shipPurchaseManager.createShipPurchaseJob(ship, ShipType.SHIP_EXPLORER)).thenReturn(shipPurchaseJob);

		// When this job gets back around to the purchase manager, throw an exception so we don't go on forever
		when(shipPurchaseManager.manageShipPurchase(shipPurchaseJob)).thenThrow(IllegalArgumentException.class);

		queue.establishJobs(List.of(tradeJob));
		assertThrows(IllegalArgumentException.class, () -> queue.beginJobQueue());
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
		final ShipJobCreator shipJobCreator = mock();
		final WebsocketReporter websocketReporter = mock();
		final ShipJobQueue queue = new ShipJobQueue(miningShipManager, tradeShipManager, shipPurchaseManager,
				shipJobCreator, websocketReporter);

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
