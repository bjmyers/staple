package org.psu.shiporchestrator;

import java.time.Duration;
import java.time.Instant;
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

	final TreeMap<Instant, ShipJob> queue;
	final AtomicReference<ShipType> shipTypeToBuy;

	@Inject
	public ShipJobQueue(final MiningShipManager miningShipManager, final TradeShipManager tradeShipManager,
			final ShipPurchaseManager shipPurchaseManager, final ShipJobCreator shipJobCreator) {
		this.miningShipManager = miningShipManager;
		this.tradeShipManager = tradeShipManager;
		this.shipPurchaseManager = shipPurchaseManager;
		this.shipJobCreator = shipJobCreator;
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
		jobs.forEach(j -> queue.put(j.getNextAction(), j));
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

			final Entry<Instant, ShipJob> jobToPerform = this.queue.pollFirstEntry();
			final Ship ship = jobToPerform.getValue().getShip();
			ShipJob nextJob = null;
			if (jobToPerform.getValue() instanceof TradeShipJob tradeJob) {
				nextJob = tradeShipManager.manageTradeShip(tradeJob);
			}
			else if (jobToPerform.getValue() instanceof MiningShipJob miningJob) {
				nextJob = miningShipManager.manageMiningShip(miningJob);
			}
			else if (jobToPerform.getValue() instanceof ShipPurchaseJob purchaseJob) {
				final ShipPurchaseManagerResponse purchaseResponse = shipPurchaseManager.manageShipPurchase(purchaseJob);
				if (purchaseResponse.nextJob() != null) {
					// The purchase job has not yet been finished
					nextJob = purchaseResponse.nextJob();
				}
				else {
					// The purchase job has finished, keeping nextJob null will result in a new job
					// for ship, but we still need to make an additional job for the new ship
					final ShipJob jobForNewShip = shipJobCreator.createShipJob(purchaseResponse.newShip());
					log.infof("Created new job for ship %s", jobForNewShip.getShip().getSymbol());
					this.queue.put(jobForNewShip.getNextAction(), jobForNewShip);
				}
			}
			else {
				log.warnf("Unknown job type, %s", jobToPerform.getValue());
			}
			if (nextJob == null) {
				// Job has finished, make a new one
				if (this.shipTypeToBuy.get() != null) {
					nextJob = shipPurchaseManager.createShipPurchaseJob(ship, this.shipTypeToBuy.get());
					log.infof("Created Job for ship %s to Purchase %s", ship.getSymbol(), this.shipTypeToBuy.get());
					this.shipTypeToBuy.set(null);
				}
				else {
					nextJob = shipJobCreator.createShipJob(ship);
				}
			}
			this.queue.put(nextJob.getNextAction(), nextJob);
		}
	}

}
