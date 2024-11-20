package org.psu.testdriver.client;

import java.util.Optional;
import java.util.stream.Collectors;

import org.psu.spacetraders.api.MarketplaceClient;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.TradeGood;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.spacetraders.dto.Transaction;
import org.psu.testdriver.LocalAgentManager;
import org.psu.testdriver.LocalMarketplaceManager;
import org.psu.testdriver.LocalShipManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

/**
 * Local version of the {@link MarketplaceClient}
 */
@JBossLog
@ApplicationScoped
public class LocalMarketplaceClient implements MarketplaceClient {

	@Inject
	private LocalMarketplaceManager marketplaceManager;

	@Inject
	private LocalShipManager shipManager;

	@Inject
	private LocalAgentManager agentManager;

	@Override
	public DataWrapper<MarketInfo> getMarketInfo(String systemId, String waypointId) {
		return new DataWrapper<>(marketplaceManager.getMarketInfo(waypointId), null);
	}

	@Override
	public DataWrapper<TradeResponse> sell(String shipId, TradeRequest tradeRequest) {
		final Ship ship = shipManager.getShip(shipId);

		// Ensure the ship is at a marketplace that purchases the good
		final String location = ship.getNav().getWaypointSymbol();
		final MarketInfo market = marketplaceManager.getMarketInfo(location);
		if (!market.sellsProduct(new Product(tradeRequest.getSymbol()))) {
			throw new IllegalStateException("Market will not purchase the specified product");
		}

		// Ensure the ship has the proper goods to sell
		final CargoItem cargoItem = ship.getCargo().getInventory().stream()
				.filter(c -> tradeRequest.getSymbol().equals(c.getSymbol())).findFirst().get();
		if (cargoItem.getUnits() < tradeRequest.getUnits()) {
			throw new IllegalStateException("Ship does not have enough units to sell");
		}

		// Update the ship's cargo
		final int newTotal = cargoItem.getUnits() - tradeRequest.getUnits();
		if (newTotal == 0) {
			ship.getCargo().getInventory().remove(cargoItem);
		}
		else {
			cargoItem.setUnits(newTotal);
		}
		ship.getCargo()
				.setUnits(ship.getCargo().getInventory().stream().collect(Collectors.summingInt(CargoItem::getUnits)));

		// Update the agent's credit total
		final TradeGood tradeGood = market.getTradeGoods().stream()
				.filter(t -> t.getSymbol().equals(tradeRequest.getSymbol())).findFirst().get();
		final int sellPrice = tradeGood.getPurchasePrice() * tradeRequest.getUnits();
		final Agent agent = agentManager.getAgent();
		final int newCreditTotal = agent.getCredits() + sellPrice;
		agent.setCredits(newCreditTotal);

		final Transaction transaction = new Transaction(location, shipId, tradeRequest.getUnits(), sellPrice);
		final TradeResponse tradeResponse = new TradeResponse(agent, ship.getCargo(), transaction);
		return new DataWrapper<TradeResponse>(tradeResponse, null);
	}

	@Override
	public DataWrapper<TradeResponse> purchase(String shipId, TradeRequest tradeRequest) {
		final Ship ship = shipManager.getShip(shipId);

		// Ensure the ship is at a marketplace that sells the good
		final String location = ship.getNav().getWaypointSymbol();
		final MarketInfo market = marketplaceManager.getMarketInfo(location);
		if (!market.sellsProduct(new Product(tradeRequest.getSymbol()))) {
			throw new IllegalStateException("Market will not sell the specified product");
		}

		// Ensure the ship has room to purchase the item
		if (tradeRequest.getUnits() > ship.getRemainingCargo()) {
			throw new IllegalStateException("Ship does not have enough room to purchase item");
		}

		// Update the ship's cargo
		final Optional<CargoItem> optionalCargoItem = ship.getCargo().getInventory().stream()
				.filter(c -> tradeRequest.getSymbol().equals(c.getSymbol())).findFirst();
		if (optionalCargoItem.isPresent()) {
			final CargoItem cargoItem = optionalCargoItem.get();
			final int newUnits = cargoItem.getUnits() + tradeRequest.getUnits();
			cargoItem.setUnits(newUnits);
		}
		else {
			ship.getCargo().getInventory().add(new CargoItem(tradeRequest.getSymbol(), tradeRequest.getUnits()));
		}
		ship.getCargo()
				.setUnits(ship.getCargo().getInventory().stream().collect(Collectors.summingInt(CargoItem::getUnits)));

		// Update the agent's credit total
		final TradeGood tradeGood = market.getTradeGoods().stream()
				.filter(t -> t.getSymbol().equals(tradeRequest.getSymbol())).findFirst().get();
		final int sellPrice = tradeGood.getSellPrice() * tradeRequest.getUnits();
		final Agent agent = agentManager.getAgent();
		final int newCreditTotal = agent.getCredits() - sellPrice;
		agent.setCredits(newCreditTotal);

		final Transaction transaction = new Transaction(location, shipId, tradeRequest.getUnits(), sellPrice);
		final TradeResponse tradeResponse = new TradeResponse(agent, ship.getCargo(), transaction);
		return new DataWrapper<TradeResponse>(tradeResponse, null);
	}

	@Override
	public DataWrapper<RefuelResponse> refuel(String shipId) {
		final Ship ship = shipManager.getShip(shipId);

		// Ensure the ship is at a marketplace that sells the good
		final String location = ship.getNav().getWaypointSymbol();
		final MarketInfo market = marketplaceManager.getMarketInfo(location);
		if (!market.sellsProduct(Product.FUEL)) {
			throw new IllegalStateException("Market will not sell fuel");
		}

		final FuelStatus currentFuelStatus = ship.getFuel();
		final FuelStatus newFuelStatus = new FuelStatus(currentFuelStatus.capacity(), currentFuelStatus.capacity());
		ship.setFuel(newFuelStatus);

		// Update the agent's credit total
		final int unitsToRefuel = currentFuelStatus.capacity() - currentFuelStatus.current();
		// Each unit of fuel is 100 units in the fuel tank, need to round up to the nearest unit of fuel
		final int unitsToPurchase = (unitsToRefuel / 100) + 1;
		final TradeGood tradeGood = market.getTradeGoods().stream()
				.filter(t -> t.getSymbol().equals(Product.FUEL.getSymbol())).findFirst().get();
		final int sellPrice = tradeGood.getSellPrice() * unitsToPurchase;
		final Agent agent = agentManager.getAgent();
		final int newCreditTotal = agent.getCredits() - sellPrice;
		agent.setCredits(newCreditTotal);

		final Transaction transaction = new Transaction(location, shipId, unitsToPurchase, sellPrice);
		final RefuelResponse refuelResponse = new RefuelResponse(agent, newFuelStatus, transaction);
		return new DataWrapper<RefuelResponse>(refuelResponse, null);
	}

}
