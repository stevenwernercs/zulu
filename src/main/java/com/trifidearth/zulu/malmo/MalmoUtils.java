package com.trifidearth.zulu.malmo;

import com.microsoft.msr.malmo.AgentHost;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
//import javax.json.*;
//import java.io.StringReader;


public class MalmoUtils {
    public enum CONTINUOUS_MOVEMENT_COMMAND {

        move, strafe, pitch, turn;

        private double upperbound;
        private double lowerbound;
        private DecimalFormat df = new DecimalFormat("#.##");

        CONTINUOUS_MOVEMENT_COMMAND() {
            this(-1d, 1d);
        }

        CONTINUOUS_MOVEMENT_COMMAND(double lowerbound, double uppperboud) {
            this.lowerbound = lowerbound;
            this.upperbound = uppperboud;
        }

        public String get(double value) {
            if(value < lowerbound || value > upperbound) {
                throw new IllegalArgumentException("Value must be within range: [" + lowerbound + ", " + upperbound + "]");
            }
            String command = name() + " " + df.format(value);
            System.out.println(command);
            return command;
        }

    }

    public enum BOOLEAN_MOVEMENT_COMMAND {

        jump, crouch, attack, use;

        public String get(boolean value) {
            String command = name() + " " + (value ? 1 : 0);
            System.out.println(command);
            return command;
        }

        public String getAsBoolean(double random) {
            return get(random >= .5d);
        }
    }

    public enum OBSERVATION {

        //long
        DistanceTravelled,
        TimeAlive,
        WorldTime,
        TotalTime,

        //int
        MobsKilled,
        PlayersKilled,
        DamageTaken,
        DamageDealt,
        Score,
        Food,
        XP,
        Air,

        //float
        Life,
        XPos,
        YPos,
        ZPos,
        Pitch,
        Yaw,

        //boolean
        IsAlive,

        //String
        Name,

        //Array
        floor3x3,

        //Inventory
		Hotbar_0_item,
		Hotbar_0_size,
		Hotbar_1_item,
		Hotbar_1_size,
		Hotbar_2_item,
		Hotbar_2_size,
		Hotbar_3_item,
		Hotbar_3_size,
		Hotbar_4_item,
		Hotbar_4_size,
		Hotbar_5_item,
		Hotbar_5_size,
		Hotbar_6_item,
		Hotbar_6_size,
		Hotbar_7_item,
		Hotbar_7_size,
		Hotbar_8_item,
		Hotbar_8_size,
		InventorySlot_0_item,
		InventorySlot_0_size,
		InventorySlot_1_item,
		InventorySlot_1_size,
		InventorySlot_2_item,
		InventorySlot_2_size,
		InventorySlot_3_item,
		InventorySlot_3_size,
		InventorySlot_4_item,
		InventorySlot_4_size,
		InventorySlot_5_item,
		InventorySlot_5_size,
		InventorySlot_6_item,
		InventorySlot_6_size,
		InventorySlot_7_item,
		InventorySlot_7_size,
		InventorySlot_8_item,
		InventorySlot_8_size,
		InventorySlot_9_item,
		InventorySlot_9_size,
		InventorySlot_10_item,
		InventorySlot_10_size,
		InventorySlot_11_item,
		InventorySlot_11_size,
		InventorySlot_12_item,
		InventorySlot_12_size,
		InventorySlot_13_item,
		InventorySlot_13_size,
		InventorySlot_14_item,
		InventorySlot_14_size,
		InventorySlot_15_item,
		InventorySlot_15_size,
		InventorySlot_16_item,
		InventorySlot_16_size,
		InventorySlot_17_item,
		InventorySlot_17_size,
		InventorySlot_18_item,
		InventorySlot_18_size,
		InventorySlot_19_item,
		InventorySlot_19_size,
		InventorySlot_20_item,
		InventorySlot_20_size,
		InventorySlot_21_item,
		InventorySlot_21_size,
		InventorySlot_22_item,
		InventorySlot_22_size,
		InventorySlot_23_item,
		InventorySlot_23_size,
		InventorySlot_24_item,
		InventorySlot_24_size,
		InventorySlot_25_item,
		InventorySlot_25_size,
		InventorySlot_26_item,
        InventorySlot_26_size,
		InventorySlot_27_item,
		InventorySlot_27_size,
		InventorySlot_28_item,
		InventorySlot_28_size,
		InventorySlot_29_item,
		InventorySlot_29_size,
		InventorySlot_30_item,
		InventorySlot_30_size,
		InventorySlot_31_item,
		InventorySlot_31_size,
		InventorySlot_32_item,
		InventorySlot_32_size,
		InventorySlot_33_item,
		InventorySlot_33_size,
		InventorySlot_34_item,
		InventorySlot_34_size,
		InventorySlot_35_item,
		InventorySlot_35_size,
		InventorySlot_36_item,
		InventorySlot_36_size,
		InventorySlot_37_item,
		InventorySlot_37_size,
		InventorySlot_38_item,
		InventorySlot_38_size,
		InventorySlot_39_item,
		InventorySlot_39_size,
		InventorySlot_40_item,
		InventorySlot_40_size,
		currentItemIndex,
		inventoriesAvailable,

		//Chat
		Chat,

        //unknown
        placeholder,


		//Propioception:
		  //boolean
		_crouch,
		_jump,
		_attack,
		_use,
		  //range
		_pitch,
		_turn,
		_strafe,
		_move,

	}


    public static double getRandomRange() {
        return getRandomRange(-1, 1);
    }

    public static double getRandomRange(double lower, double upper) {
        return Math.random()*(upper - lower) + lower;
    }

	public static boolean coinTossWeighted(double percentage) {
    	return Math.random() < percentage;
	}
    public static boolean coinToss() {
    	return coinTossWeighted(.5d);
	}

    public static Map<OBSERVATION, String> pasrseObservationJson(String observationJson) {

        Map<OBSERVATION,String> observations = new TreeMap<>();

        if (observationJson == null || observationJson.isEmpty()) {
            return observations;
        }

        /*
        JsonReader reader = Json.createReader(new StringReader(observationJson));
        JsonStructure jsonStructure = reader.read();

        JsonObject observationObject = (JsonObject) jsonStructure;

        for (Map.Entry<String, JsonValue> observation : observationObject.entrySet()) {
        */

        JSONObject observationJsonObject = new JSONObject(observationJson);
        for (String keyString : observationJsonObject.keySet()) {

        OBSERVATION key;
        try {
            key = OBSERVATION.valueOf(keyString);
        } catch (IllegalArgumentException ex) {
            System.out.println("UNKNOWN OBSERVATION: " + keyString);
            //key= OBSERVATION.placeholder;
            throw new RuntimeException("UNKNOWN OBSERVATION: " + keyString, ex);
        }
            String valueString = observationJsonObject.get(keyString).toString();
            observations.put(key,valueString);
        }

        return observations;
    }

    public static void randomBehavior( Map<MalmoUtils.OBSERVATION,String> observations, AgentHost agent_host) {

		{
			boolean jumpState = observations.get(MalmoUtils.OBSERVATION._jump).equals("1");
			boolean crouchState = observations.get(MalmoUtils.OBSERVATION._crouch).equals("1");
			if ((jumpState || crouchState) && MalmoUtils.coinTossWeighted(.3d)) {
				agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.jump.get(false));
				observations.put(MalmoUtils.OBSERVATION._jump, "0");
				agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.crouch.get(false));
				observations.put(MalmoUtils.OBSERVATION._crouch, "0");
			} else if (MalmoUtils.coinTossWeighted(.02d)) {
				if (MalmoUtils.coinTossWeighted(.95)) {
					agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.jump.get(true));
					observations.put(MalmoUtils.OBSERVATION._jump, "1");
				} else {
					agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.crouch.get(true));
					observations.put(MalmoUtils.OBSERVATION._crouch, "1");
				}
			}
		}

		{
			boolean useState = observations.get(MalmoUtils.OBSERVATION._use).equals("1");
			boolean attackState = observations.get(MalmoUtils.OBSERVATION._attack).equals("1");
			if ((useState || attackState) && MalmoUtils.coinTossWeighted(.03d)) {
				agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.attack.get(false));
				observations.put(MalmoUtils.OBSERVATION._attack, "0");
				agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.use.get(false));
				observations.put(MalmoUtils.OBSERVATION._use, "0");
			} else if (MalmoUtils.coinTossWeighted(.3d)) {
				if (MalmoUtils.coinTossWeighted(.5d)) {
					agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.attack.get(true));
					observations.put(MalmoUtils.OBSERVATION._attack, "1");
				} else {
					agent_host.sendCommand(MalmoUtils.BOOLEAN_MOVEMENT_COMMAND.use.get(true));
					observations.put(MalmoUtils.OBSERVATION._use, "1");
				}
			}
		}

		{
			boolean strafeState = !observations.get(MalmoUtils.OBSERVATION._strafe).equals("0");
			if (strafeState && MalmoUtils.coinTossWeighted(.7d)) {
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.strafe.get(0));
				observations.put(MalmoUtils.OBSERVATION._strafe, "0");
			} else if (MalmoUtils.coinTossWeighted(.20d)) {
				double value = MalmoUtils.getRandomRange();
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.strafe.get(value));
				observations.put(MalmoUtils.OBSERVATION._strafe, value + "");
			}
		}

		{
			boolean moveState = !observations.get(MalmoUtils.OBSERVATION._move).equals("0");
			if (moveState && MalmoUtils.coinTossWeighted(.3d)) {
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.move.get(0));
				observations.put(MalmoUtils.OBSERVATION._move, "0");
			} else if (MalmoUtils.coinTossWeighted(.50d)) {
				double value = MalmoUtils.getRandomRange();
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.move.get(value));
				observations.put(MalmoUtils.OBSERVATION._move, value + "");
			}
		}

		{
			boolean pitchState = !observations.get(MalmoUtils.OBSERVATION._pitch).equals("0");
			if (pitchState && MalmoUtils.coinTossWeighted(.7d)) {
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.pitch.get(0));
				observations.put(MalmoUtils.OBSERVATION._pitch, "0");
			} else if (MalmoUtils.coinTossWeighted(.20d)) {
				String pitchValueString = observations.get(MalmoUtils.OBSERVATION.Pitch);
				double value = MalmoUtils.getRandomRange();
				if(pitchValueString != null) {
					double pitchValue = Double.parseDouble(pitchValueString);
					if(pitchValue > 30) {
						value = MalmoUtils.getRandomRange(-1, 0);
					} else if(pitchValue < -30) {
						value = MalmoUtils.getRandomRange(0, 1);
					}
				}
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.pitch.get(value));
				observations.put(MalmoUtils.OBSERVATION._pitch, value + "");
			}
		}

		{
			boolean turnState = !observations.get(MalmoUtils.OBSERVATION._turn).equals("0");
			if (turnState && MalmoUtils.coinTossWeighted(.7d)) {
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.turn.get(0));
				observations.put(MalmoUtils.OBSERVATION._turn, "0");
			} else if (MalmoUtils.coinTossWeighted(.20d)) {
				double value = MalmoUtils.getRandomRange();
				agent_host.sendCommand(MalmoUtils.CONTINUOUS_MOVEMENT_COMMAND.turn.get(value));
				observations.put(MalmoUtils.OBSERVATION._turn, value + "");
			}
		}
	}
}