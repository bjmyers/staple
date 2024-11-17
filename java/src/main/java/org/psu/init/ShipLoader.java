package org.psu.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.psu.miningmanager.MiningShipManager;
import org.psu.miningmanager.MiningSiteManager;
import org.psu.navigation.RefuelPathCalculator;
import org.psu.shiporchestrator.ShipJob;
import org.psu.shiporchestrator.ShipJobQueue;
import org.psu.shiporchestrator.ShipRoleManager;
import org.psu.spacetraders.api.ClientProducer;
import org.psu.spacetraders.api.RequestThrottler;
import org.psu.spacetraders.api.ShipsClient;
import org.psu.spacetraders.api.SpaceTradersUtils;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.DataWrapper.WrapperMetadata;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.Waypoint;
import org.psu.trademanager.MarketplaceManager;
import org.psu.trademanager.TradeShipManager;
import org.psu.websocket.WebsocketReporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Loads the user's ships
 */
@JBossLog
@ApplicationScoped
public class ShipLoader {

	private int limit;
	private final ShipsClient shipsClient;
	private final RequestThrottler throttler;
	private final SystemBuilder systemBuilder;
	private final ShipRoleManager shipRoleManager;
	private final MiningShipManager miningShipManager;
	private final TradeShipManager tradeShipManager;
	private final MarketplaceManager marketplaceManager;
	private final MiningSiteManager miningSiteManager;
	private final RefuelPathCalculator refuelPathCalculator;
	private final ShipJobQueue jobQueue;
	private final WebsocketReporter websocketReporter;

	@Inject
	public ShipLoader(@ConfigProperty(name = "app.max-items-per-page") final int limit,
			final ClientProducer clientProducer, final RequestThrottler throttler,
			final SystemBuilder systemBuilder, final ShipRoleManager shipRoleManager,
			final MiningShipManager miningShipManager, final TradeShipManager tradeShipManager,
			final MarketplaceManager marketplaceManager, final MiningSiteManager miningSiteManager,
			final ShipJobQueue jobQueue, final RefuelPathCalculator refuelPathCalculator,
			final WebsocketReporter websocketReporter) {
		this.limit = limit;
		this.shipsClient = clientProducer.produceShipsClient();
		this.throttler = throttler;
		this.systemBuilder = systemBuilder;
		this.shipRoleManager = shipRoleManager;
		this.miningShipManager = miningShipManager;
		this.tradeShipManager = tradeShipManager;
		this.marketplaceManager = marketplaceManager;
		this.miningSiteManager = miningSiteManager;
		this.jobQueue = jobQueue;
		this.refuelPathCalculator = refuelPathCalculator;
		this.websocketReporter = websocketReporter;
	}

	/**
	 * This method queries the space traders API for system information
	 */
    public void run() {
    	log.info("Initializing the Ships");

		final List<Ship> ships = gatherShips();

		ships.forEach(s -> log.infof("Found ship %s with type %s", s.getSymbol(), shipRoleManager.determineRole(s)));

		final Map<String, Long> shipCountBySystem = ships.stream().map(Ship::getNav)
				.collect(Collectors.groupingBy(ShipNavigation::getSystemSymbol, Collectors.counting()));

		final Optional<Entry<String, Long>> primarySystem = shipCountBySystem.entrySet().stream()
				.max(Map.Entry.comparingByValue());

		if (primarySystem.isEmpty()) {
			// User has no ships
			throw new IllegalStateException(
					"No ships found for user, STAPLE requires that the user owns at least one ship");
		}
		final String primarySystemId = primarySystem.get().getKey();
		log.infof("Determined Primary System: %s", primarySystemId);

    	final List<Waypoint> systemWaypoints = systemBuilder.gatherWaypoints(primarySystemId);

    	log.infof("Found %s Waypoints", systemWaypoints.size());

    	miningSiteManager.addSites(systemWaypoints);

    	log.info("Gathering Market Info");
    	final Map<Waypoint, MarketInfo> marketInfo = systemBuilder.gatherMarketInfo(systemWaypoints);
    	marketplaceManager.updateMarketData(marketInfo);
    	log.infof("Found Market Info for %s marketplaces", marketInfo.size());

    	final List<Waypoint> waypointsWhichTradeFuel = marketInfo.entrySet().stream()
    			.filter(e -> e.getValue().sellsProduct(Product.FUEL)).map(Entry::getKey).toList();
    	refuelPathCalculator.loadRefuelWaypoints(waypointsWhichTradeFuel);

		final List<ShipJob> jobs = new ArrayList<>();
		for (final Ship ship : ships) {
			switch (shipRoleManager.determineRole(ship)) {
			case MINING:
				jobs.add(miningShipManager.createJob(ship));
				break;
			case TRADE:
				jobs.add(tradeShipManager.createJob(ship));
				break;
			case PROBE:
				break;
			}
		}

		jobQueue.establishJobs(jobs);
		jobQueue.beginJobQueue();
	}

    public List<Ship> gatherShips() {
		final DataWrapper<List<Ship>> initialPage = throttler.throttle(() -> shipsClient.getShips(limit, 1));
		log.info("Gathered ship page 1");

		final WrapperMetadata metaData = initialPage.getMeta();
		final int numPages = SpaceTradersUtils.getTotalNumPages(metaData);

		final List<Ship> ships = new ArrayList<>(metaData.getTotal());
		ships.addAll(initialPage.getData());

		for (int i = 2; i < numPages + 1; i++) {
			// Make this final so it can be given to the throttler
			final int page = i;

			final DataWrapper<List<Ship>> nextPage = throttler.throttle(() -> shipsClient.getShips(limit, page));
			log.infof("Gathered ship page %s", i);
			ships.addAll(nextPage.getData());
		}
		websocketReporter.updateShips(ships);

		return ships;
    }

}
