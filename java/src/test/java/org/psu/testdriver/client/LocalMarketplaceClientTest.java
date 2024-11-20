package org.psu.testdriver.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.psu.spacetraders.dto.Agent;
import org.psu.spacetraders.dto.Cargo;
import org.psu.spacetraders.dto.CargoItem;
import org.psu.spacetraders.dto.DataWrapper;
import org.psu.spacetraders.dto.FuelStatus;
import org.psu.spacetraders.dto.MarketInfo;
import org.psu.spacetraders.dto.Product;
import org.psu.spacetraders.dto.RefuelResponse;
import org.psu.spacetraders.dto.Ship;
import org.psu.spacetraders.dto.ShipNavigation;
import org.psu.spacetraders.dto.TradeGood;
import org.psu.spacetraders.dto.TradeRequest;
import org.psu.spacetraders.dto.TradeResponse;
import org.psu.testdriver.LocalAgentManager;
import org.psu.testdriver.LocalMarketplaceManager;
import org.psu.testdriver.LocalShipManager;

/**
 * Tests for {@link LocalMarketplaceClient}
 */
@ExtendWith(MockitoExtension.class)
public class LocalMarketplaceClientTest {

	@Mock
	private LocalMarketplaceManager marketplaceManager;

	@Mock
	private LocalShipManager shipManager;

	@Mock
	private LocalAgentManager agentManager;

	@InjectMocks
	private LocalMarketplaceClient localMarketplaceClient;

	/**
	 * Tests the getMarketInfo method
	 */
	@Test
	public void getMarketInfo() {
		final String marketId = "market";
		final MarketInfo market = mock();
		when(marketplaceManager.getMarketInfo(marketId)).thenReturn(market);

		final DataWrapper<MarketInfo> response = localMarketplaceClient.getMarketInfo("system", marketId);
		assertNull(response.getMeta());
		assertEquals(market, response.getData());
	}

	/**
	 * Tests sell for a market which does not purchase the product
	 */
	@Test
	public void sellDoesNotPurchase() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(false);
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final TradeRequest tradeRequest = new TradeRequest(productId, 1);

		assertThrows(IllegalStateException.class, () -> localMarketplaceClient.sell(shipId, tradeRequest));
	}

	/**
	 * Tests sell for a ship which doesn't have enough units to sell
	 */
	@Test
	public void sellNotEnoughUnits() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final CargoItem cargoItem = new CargoItem(productId, 1);
		final CargoItem cargoItem2 = new CargoItem("some other product", 1);
		// Need this list to be mutable
		final List<CargoItem> cargoItems = new ArrayList<>();
		cargoItems.add(cargoItem);
		cargoItems.add(cargoItem2);
		final Cargo cargo = new Cargo(100, 2, cargoItems);
		when(ship.getCargo()).thenReturn(cargo);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		// The ship only has one unit of product
		final TradeRequest tradeRequest = new TradeRequest(productId, 10);

		assertThrows(IllegalStateException.class, () -> localMarketplaceClient.sell(shipId, tradeRequest));
	}

	/**
	 * Tests sell for a ship which is selling all that it has of a product
	 */
	@Test
	public void sellAll() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final CargoItem cargoItem = new CargoItem(productId, 2);
		final CargoItem cargoItem2 = new CargoItem("some other product", 1);
		// Need this list to be mutable
		final List<CargoItem> cargoItems = new ArrayList<>();
		cargoItems.add(cargoItem);
		cargoItems.add(cargoItem2);
		final Cargo cargo = new Cargo(100, 3, cargoItems);
		when(ship.getCargo()).thenReturn(cargo);

		final TradeGood tradeGood = new TradeGood(productId, 100, 10, 20);
		final TradeGood tradeGood2 = new TradeGood("some other product", 100, 10, 30);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(market.getTradeGoods()).thenReturn(List.of(tradeGood, tradeGood2));
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final Agent agent = mock();
		when(agent.getCredits()).thenReturn(1000);
		when(agentManager.getAgent()).thenReturn(agent);

		// The ship only has one unit of product
		final TradeRequest tradeRequest = new TradeRequest(productId, 2);

		final DataWrapper<TradeResponse> tradeResponse = localMarketplaceClient.sell(shipId, tradeRequest);

		assertNull(tradeResponse.getMeta());
		final TradeResponse response = tradeResponse.getData();

		// The product has been removed from the ship's inventory
		assertEquals(1, ship.getCargo().getInventory().size());
		assertEquals(1, ship.getCargo().getUnits());

		// Had 1000 credits, sold two units at 10 credits each
		verify(agent).setCredits(1020);

		assertEquals(agent, response.getAgent());
		assertEquals(cargo, response.getCargo());
		assertEquals(shipId, response.getTransaction().getShipSymbol());
		assertEquals(20, response.getTransaction().getTotalPrice());
		assertEquals(2, response.getTransaction().getUnits());
		assertEquals(wayId, response.getTransaction().getWaypointSymbol());
	}

	/**
	 * Tests sell for a ship which is selling some that it has of a product
	 */
	@Test
	public void sellSome() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final CargoItem cargoItem = new CargoItem(productId, 10);
		final CargoItem cargoItem2 = new CargoItem("some other product", 1);
		// Need this list to be mutable
		final List<CargoItem> cargoItems = new ArrayList<>();
		cargoItems.add(cargoItem);
		cargoItems.add(cargoItem2);
		final Cargo cargo = new Cargo(100, 11, cargoItems);
		when(ship.getCargo()).thenReturn(cargo);

		final TradeGood tradeGood = new TradeGood(productId, 100, 10, 20);
		final TradeGood tradeGood2 = new TradeGood("some other product", 100, 10, 30);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(market.getTradeGoods()).thenReturn(List.of(tradeGood, tradeGood2));
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final Agent agent = mock();
		when(agent.getCredits()).thenReturn(1000);
		when(agentManager.getAgent()).thenReturn(agent);

		// The ship only has one unit of product
		final TradeRequest tradeRequest = new TradeRequest(productId, 2);

		final DataWrapper<TradeResponse> tradeResponse = localMarketplaceClient.sell(shipId, tradeRequest);

		assertNull(tradeResponse.getMeta());
		final TradeResponse response = tradeResponse.getData();

		// Some of the product has been removed from the ship's inventory
		assertEquals(2, ship.getCargo().getInventory().size());
		assertEquals(9, ship.getCargo().getUnits());

		// Had 1000 credits, sold two units at 10 credits each
		verify(agent).setCredits(1020);

		assertEquals(agent, response.getAgent());
		assertEquals(cargo, response.getCargo());
		assertEquals(shipId, response.getTransaction().getShipSymbol());
		assertEquals(20, response.getTransaction().getTotalPrice());
		assertEquals(2, response.getTransaction().getUnits());
		assertEquals(wayId, response.getTransaction().getWaypointSymbol());
	}

	/**
	 * Tests purchase for a market which does not sell the product
	 */
	@Test
	public void purchaseDoesNotSell() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(false);
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final TradeRequest tradeRequest = new TradeRequest(productId, 1);

		assertThrows(IllegalStateException.class, () -> localMarketplaceClient.purchase(shipId, tradeRequest));
	}

	/**
	 * Tests purchase for a ship which doesn't have space to hold the goods
	 */
	@Test
	public void purchaseNotEnoughSpace() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);
		when(ship.getRemainingCargo()).thenReturn(0);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final TradeRequest tradeRequest = new TradeRequest(productId, 10);

		assertThrows(IllegalStateException.class, () -> localMarketplaceClient.purchase(shipId, tradeRequest));
	}

	/**
	 * Tests purchase when the ship already has some of the product in its cargo
	 */
	@Test
	public void purchaseHasItem() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final CargoItem cargoItem = new CargoItem(productId, 2);
		final CargoItem cargoItem2 = new CargoItem("some other product", 1);
		final List<CargoItem> cargoItems = new ArrayList<>();
		cargoItems.add(cargoItem);
		cargoItems.add(cargoItem2);
		final Cargo cargo = new Cargo(100, 3, cargoItems);
		when(ship.getCargo()).thenReturn(cargo);
		when(ship.getRemainingCargo()).thenReturn(97);

		final TradeGood tradeGood = new TradeGood(productId, 100, 10, 20);
		final TradeGood tradeGood2 = new TradeGood("some other product", 100, 10, 30);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(market.getTradeGoods()).thenReturn(List.of(tradeGood, tradeGood2));
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final Agent agent = mock();
		when(agent.getCredits()).thenReturn(1000);
		when(agentManager.getAgent()).thenReturn(agent);

		// The ship purchases 2 more units
		final TradeRequest tradeRequest = new TradeRequest(productId, 2);

		final DataWrapper<TradeResponse> tradeResponse = localMarketplaceClient.purchase(shipId, tradeRequest);

		assertNull(tradeResponse.getMeta());
		final TradeResponse response = tradeResponse.getData();

		assertEquals(2, ship.getCargo().getInventory().size());
		assertEquals(5, ship.getCargo().getUnits());

		// Had 1000 credits, purchased two units at 20 credits each
		verify(agent).setCredits(960);

		assertEquals(agent, response.getAgent());
		assertEquals(cargo, response.getCargo());
		assertEquals(shipId, response.getTransaction().getShipSymbol());
		assertEquals(40, response.getTransaction().getTotalPrice());
		assertEquals(2, response.getTransaction().getUnits());
		assertEquals(wayId, response.getTransaction().getWaypointSymbol());
	}

	/**
	 * Tests purchase when the ship doesn't have any of the item in its cargo
	 */
	@Test
	public void purchaseDoesntHaveItem() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final String productId = "freedom";
		final Product product = new Product(productId);

		final List<CargoItem> cargoItems = new ArrayList<>();
		final Cargo cargo = new Cargo(100, 0, cargoItems);
		when(ship.getCargo()).thenReturn(cargo);
		when(ship.getRemainingCargo()).thenReturn(100);

		final TradeGood tradeGood = new TradeGood(productId, 100, 10, 20);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(market.getTradeGoods()).thenReturn(List.of(tradeGood));
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final Agent agent = mock();
		when(agent.getCredits()).thenReturn(1000);
		when(agentManager.getAgent()).thenReturn(agent);

		// The ship purchases 5 more units
		final TradeRequest tradeRequest = new TradeRequest(productId, 5);

		final DataWrapper<TradeResponse> tradeResponse = localMarketplaceClient.purchase(shipId, tradeRequest);

		assertNull(tradeResponse.getMeta());
		final TradeResponse response = tradeResponse.getData();

		// The product has been added to the ship's inventory
		assertEquals(1, ship.getCargo().getInventory().size());
		assertEquals(5, ship.getCargo().getUnits());

		// Had 1000 credits, purchased 5 units at 20 credits each
		verify(agent).setCredits(900);

		assertEquals(agent, response.getAgent());
		assertEquals(cargo, response.getCargo());
		assertEquals(shipId, response.getTransaction().getShipSymbol());
		assertEquals(100, response.getTransaction().getTotalPrice());
		assertEquals(5, response.getTransaction().getUnits());
		assertEquals(wayId, response.getTransaction().getWaypointSymbol());
	}

	/**
	 * Tests refuel for a market which does not sell fuel
	 */
	@Test
	public void refuelDoesNotSell() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final MarketInfo market = mock();
		when(market.sellsProduct(Product.FUEL)).thenReturn(false);
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		assertThrows(IllegalStateException.class, () -> localMarketplaceClient.refuel(shipId));
	}

	/**
	 * Tests refuel
	 */
	@Test
	public void refuel() {

		final String wayId = "waypoint";

		final Ship ship = mock();
		final ShipNavigation shipNav = mock();
		when(shipNav.getWaypointSymbol()).thenReturn(wayId);
		when(ship.getNav()).thenReturn(shipNav);

		final String shipId = "shippy";
		when(shipManager.getShip(shipId)).thenReturn(ship);

		final Product product = Product.FUEL;
		final String fuelId = product.getSymbol();

		// Will need to purchase two units of fuel to reach the capacity, since each
		// unit is 100 in the FuelStatus
		final FuelStatus fuelStatus = new FuelStatus(199, 300);
		when(ship.getFuel()).thenReturn(fuelStatus);

		final TradeGood tradeGood = new TradeGood(fuelId, 100, 10, 20);

		final MarketInfo market = mock();
		when(market.sellsProduct(product)).thenReturn(true);
		when(market.getTradeGoods()).thenReturn(List.of(tradeGood));
		when(marketplaceManager.getMarketInfo(wayId)).thenReturn(market);

		final Agent agent = mock();
		when(agent.getCredits()).thenReturn(1000);
		when(agentManager.getAgent()).thenReturn(agent);

		final DataWrapper<RefuelResponse> refuelResponse = localMarketplaceClient.refuel(shipId);

		assertNull(refuelResponse.getMeta());
		final RefuelResponse response = refuelResponse.getData();

		final FuelStatus expectedFuelStatus = new FuelStatus(300, 300);

		// The product has been added to the ship's inventory
		verify(ship).setFuel(expectedFuelStatus);

		// Had 1000 credits, purchased 2 units at 20 credits each
		verify(agent).setCredits(960);

		assertEquals(agent, response.getAgent());
		assertEquals(expectedFuelStatus, response.getFuel());
		assertEquals(shipId, response.getTransaction().getShipSymbol());
		assertEquals(40, response.getTransaction().getTotalPrice());
		assertEquals(2, response.getTransaction().getUnits());
		assertEquals(wayId, response.getTransaction().getWaypointSymbol());
	}

}
