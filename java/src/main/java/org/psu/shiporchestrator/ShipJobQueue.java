package org.psu.shiporchestrator;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Manages the job state for each ship, responsible for scheduling and executing jobs
 */
@JBossLog
@ApplicationScoped
public class ShipJobQueue {

	private MiningShipManager miningShipManager;
	private TradeShipManager tradeShipManager;
	private ShipPurchaseManager shipPurchaseManager;
	private ShipJobCreator shipJobCreator;
	private WebsocketReporter websocketReporter;

	final TreeMap<Instant, Deque<ShipJob>> queue;
	final AtomicReference<ShipType> shipTypeToBuy;

	@Inject
	public ShipJobQueue(final MiningShipManager miningShipManager, final TradeShipManager tradeShipManager,
			final ShipPurchaseManager shipPurchaseManager, final ShipJobCreator shipJobCreator,
			final WebsocketReporter websocketReporter) {
		this.miningShipManager = miningShipManager;
		this.tradeShipManager = tradeShipManager;
		this.shipPurchaseManager = shipPurchaseManager;
		this.shipJobCreator = shipJobCreator;
		this.websocketReporter = websocketReporter;
		this.queue = new TreeMap<>();
		this.shipTypeToBuy = new AtomicReference<ShipType>();
	}

	/**
	 * Tells the job queue to create a {@link ShipPurchaseJob} the next time a ship
	 * finishes a job
	 *
	 * @param type the type of ship to buy
	 */
	public void setShipTypeToBuy(final ShipType type) {
		this.shipTypeToBuy.set(type);
	}

	/**
	 * @param jobs The jobs to load into the job queue
	 */
	public void establishJobs(final List<? extends ShipJob> jobs) {
		log.infof("Loading %s jobs into the queue", jobs.size());
		jobs.forEach(j -> queue.computeIfAbsent(j.getNextAction(), i -> new LinkedList<>()).add(j));
	}

	public void beginJobQueue() {
		if (queue.isEmpty()) {
			throw new IllegalStateException("The job queue must contain jobs");
		}
		while (true) {
			log.debugf("Queue: %s", queue);
			final Instant nextAction = this.queue.firstKey();
			final Duration timeUntilNextAction = Duration.between(Instant.now(), nextAction);
			log.debugf("Time until next action: %s", timeUntilNextAction);
			if (timeUntilNextAction.isPositive()) {
				// Next action is in the future, sleep until its ready
				try {
					Thread.sleep(timeUntilNextAction);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			final Entry<Instant, Deque<ShipJob>> entryToPerform = this.queue.pollFirstEntry();
			final ShipJob jobToPerform = entryToPerform.getValue().pop();
			if (!entryToPerform.getValue().isEmpty()) {
				// Put it back into the queue
				this.queue.put(entryToPerform.getKey(), entryToPerform.getValue());
			}
			final Ship ship = jobToPerform.getShip();
			ShipJob nextJob = null;
			if (jobToPerform instanceof TradeShipJob tradeJob) {
				nextJob = tradeShipManager.manageTradeShip(tradeJob);
			}
			else if (jobToPerform instanceof MiningShipJob miningJob) {
				nextJob = miningShipManager.manageMiningShip(miningJob);
			}
			else if (jobToPerform instanceof ShipPurchaseJob purchaseJob) {
				final ShipPurchaseManagerResponse purchaseResponse = shipPurchaseManager.manageShipPurchase(purchaseJob);
				if (purchaseResponse.nextJob() != null) {
					// The purchase job has not yet been finished
					nextJob = purchaseResponse.nextJob();
				}
				else {
					// The purchase job has finished, keeping nextJob null will result in a new job
					// for ship, but we still need to make an additional job for the new ship
					websocketReporter.addShip(purchaseResponse.newShip());
					websocketReporter
							.firePurchaseStatusEvent("Purchased Ship " + purchaseResponse.newShip().getSymbol());
					final ShipJob jobForNewShip = shipJobCreator.createShipJob(purchaseResponse.newShip());
					log.infof("Created new job for ship %s", jobForNewShip.getShip().getSymbol());
					this.queue.computeIfAbsent(jobForNewShip.getNextAction(), i -> new LinkedList<>())
							.add(jobForNewShip);
				}
			}
			else {
				log.warnf("Unknown job type, %s", jobToPerform);
			}
			if (nextJob == null) {
				// Job has finished, make a new one
				if (this.shipTypeToBuy.get() != null) {
					nextJob = shipPurchaseManager.createShipPurchaseJob(ship, this.shipTypeToBuy.get());
					log.infof("Created Job for ship %s to Purchase %s", ship.getSymbol(), this.shipTypeToBuy.get());
					websocketReporter.firePurchaseStatusEvent(
							"Ship " + ship.getSymbol() + " beginning job to purchase new ship");
					this.shipTypeToBuy.set(null);
				}
				else {
					nextJob = shipJobCreator.createShipJob(ship);
				}
			}
			this.queue.computeIfAbsent(nextJob.getNextAction(), i -> new LinkedList<>()).add(nextJob);
		}
	}

}
