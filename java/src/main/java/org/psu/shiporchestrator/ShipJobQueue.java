package org.psu.shiporchestrator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.dto.MiningShipJob;
import org.psu.shippurchase.ShipPurchaseJob;
import org.psu.shippurchase.ShipPurchaseManager;
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

	final TreeMap<Instant, ShipJob> queue;

	@Inject
	public ShipJobQueue(final MiningShipManager miningShipManager, final TradeShipManager tradeShipManager,
			final ShipPurchaseManager shipPurchaseManager) {
		this.miningShipManager = miningShipManager;
		this.tradeShipManager = tradeShipManager;
		this.shipPurchaseManager = shipPurchaseManager;
		this.queue = new TreeMap<>();
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
			ShipJob nextJob = null;
			if (jobToPerform.getValue() instanceof TradeShipJob tradeJob) {
				nextJob = tradeShipManager.manageTradeShip(tradeJob);
			}
			else if (jobToPerform.getValue() instanceof MiningShipJob miningJob) {
				nextJob = miningShipManager.manageMiningShip(miningJob);
			}
			else if (jobToPerform.getValue() instanceof ShipPurchaseJob purchaseJob) {
				nextJob = shipPurchaseManager.manageShipPurchase(purchaseJob);
			}
			else {
				log.warnf("Unknown job type, %s", jobToPerform.getValue());
			}
			this.queue.put(nextJob.getNextAction(), nextJob);
		}
	}

}
