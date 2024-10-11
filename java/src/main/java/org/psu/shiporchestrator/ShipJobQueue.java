package org.psu.shiporchestrator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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

	private TradeShipManager tradeShipManager;

	//TODO: Make a Job interface so this can handle more than just trade ship jobs
	final TreeMap<Instant, TradeShipJob> queue;

	@Inject
	public ShipJobQueue(final TradeShipManager tradeShipManager) {
		this.tradeShipManager = tradeShipManager;
		this.queue = new TreeMap<>();
	}

	/**
	 * @param jobs The jobs to load into the job queue
	 */
	public void establishJobs(final List<TradeShipJob> jobs) {
		log.infof("Loading %s jobs into the queue", jobs.size());
		jobs.forEach(j -> queue.put(j.getNextAction(), j));
	}

	public void beginJobQueue() {
		if (queue.isEmpty()) {
			throw new IllegalStateException("The job queue must contain jobs");
		}
		while (true) {
			final Instant nextAction = this.queue.firstKey();
			final Duration timeUntilNextAction = Duration.between(Instant.now(), nextAction);
			log.infof("Time until next action: %s", timeUntilNextAction);
			if (timeUntilNextAction.isPositive()) {
				// Next action is in the future, sleep until its ready
				try {
					Thread.sleep(timeUntilNextAction);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			final Entry<Instant, TradeShipJob> jobToPerform = this.queue.pollFirstEntry();
			final TradeShipJob nextJob = tradeShipManager.manageTradeShip(jobToPerform.getValue());
			this.queue.put(nextJob.getNextAction(), nextJob);
		}
	}

}
