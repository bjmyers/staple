package org.psu.spacetraders.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

/**
 * A trait of a Waypoint
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Trait {

	private Type symbol;

	/**
	 * Enum describing the type of a trait
	 */
	@JBossLog
	public static enum Type {
		ASH_CLOUDS(true, false),
		BARREN(false, false),
		BREATHABLE_ATMOSPHERE(false, false),
		COMMON_METAL_DEPOSITS(false, true),
		CORROSIVE_ATMOSPHERE(true, false),
		DEBRIS_CLUSTER(true, false),
		DEEP_CRATERS(false, true),
		DIVERSE_LIFE(false, false),
		DRY_SEABEDS(false, false),
		EXTREME_PRESSURE(true, false),
		EXTREME_TEMPERATURES(true, false),
		HIGH_TECH(false, false),
		ICE_CRYSTALS(false, true),
		INDUSTRIAL(false, false),
		EXPLOSIVE_GASES(true, false),
		FOSSILS(false, true),
		FROZEN(false, true),
		HOLLOWED_INTERIOR(false, false),
		MARKETPLACE(false, false),
		MICRO_GRAVITY_ANOMALIES(true, false),
		MILITARY_BASE(false, false),
		MINERAL_DEPOSITS(false, true),
		MEGA_STRUCTURES(false, false),
		PERPETUAL_OVERCAST(true, false),
		PIRATE_BASE(true, false),
		PRECIOUS_METAL_DEPOSITS(false, true),
		RADIOACTIVE(true, false),
		RARE_METAL_DEPOSITS(false, true),
		ROCKY(false, true),
		SALT_FLATS(false, false),
		SCARCE_LIFE(false, false),
		SCATTERED_SETTLEMENTS(false, false),
		SHALLOW_CRATERS(false, true),
		SHIPYARD(false, false),
		SPRAWLING_CITIES(false, false),
		STRIPPED(false, false),
		STRONG_GRAVITY(true, false),
		STRONG_MAGNETOSPHERE(true, false),
		TEMPERATE(false, false),
		THIN_ATMOSPHERE(false, false),
		TOXIC_ATMOSPHERE(true, false),
		UNSTABLE_COMPOSITION(true, false),
		OUTPOST(false, false),
		VIBRANT_AURORAS(false, false),
		VOLCANIC(true, false),
		UNKNOWN(true, false);


		private final boolean isDangerous;
		private final boolean isValuable;

		Type(boolean isDangerous, boolean isValuable) {
	        this.isDangerous = isDangerous;
	        this.isValuable = isValuable;
	    }

	    public boolean isDangerous() {
	        return isDangerous;
	    }

	    public boolean isValuable() {
	    	return isValuable;
	    }

	    @JsonCreator
	    public static Type fromString(String value) {
	        for (Type type : Type.values()) {
	            if (type.name().equalsIgnoreCase(value)) {
	                return type;
	            }
	        }
			// Default
			log.warn("Unable to deserialize value: " + value + " using UKNOWN");
			return UNKNOWN;
	    }

	    @JsonValue
	    public String toValue() {
	        return this.name();
	    }
	}
}
