/*================================================================================
* CS544 - Computer Networks
* Drexel University, Spring 2017
* Protocol Implementation: Remote Control Drone Protocol
* Team 4:
* - Ajinkya Dhage
* - Ethan Shafer
* - Brent Varga
* - Xiaxin Xin
* --------------------------------------------------------------------------------
* File name: TesterDFA.java
*
* Description:
* This class provides a testing scenario demonstrate the authentication functionality.
*
* Requirements (Additional details can be found in the file below):
* - CLIENT
*
*=================================================================================
* */

package DFA;

import org.json.simple.JSONObject;

import Drone.DroneServer;
import Drone.UIDrone;
import Messages.ControlMessage;
import Messages.Message;
import Messages.ControlMessage.ControlType;
import Messages.Message.MessageType;

public class TestDFA {
	public static void main(String[] args) throws Exception {
		/**
		 * Steps to be followed for authenticating a receiver hello message and receiver response hello
		 */
		byte commandByte = 0x00;
		JSONObject json = new JSONObject();
		json.put("password", "pass");
		json.put("version", "1.2");
		json.put("random number A", "2.3456");
		ControlMessage controlMessage = new ControlMessage(ControlType.GROUNDED, commandByte, json);
		Message message = new Message(MessageType.CONTROL, 3, controlMessage);
		
		DroneServer droneServer = new DroneServer("8080", "pass", "1234", new UIDrone());
		DroneDFA droneDFA = new DroneDFA("password", "1.2", "1.23", "2.36");
		DFAResponse droneResponse = droneDFA.authenticate(message);

		/**
		 * Steps to be followed for authenticating a drone hello message
 		 */
		json.put("random number B", "2.3456");
//		ReceiverClient receiverClient = new ReceiverClient(new Socket(), new UIReceiver(),"pass");
		ReceiverDFA receiverDFA = new ReceiverDFA("password", "1.2", "1.23");
		DFAResponse receiverResponse = receiverDFA.authenticate(message);

		/**
		 * Steps to be followed for getting next DFA state
		 */
		ControlType currentState = ControlType.PREFLIGHT;
		DFAResponse nextState = DFAState.getNextState(message, currentState);
	}

}
