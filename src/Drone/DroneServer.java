package Drone;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

import DFA.DFAResponse;
import DFA.DFAState;
import DFA.DroneDFA;
import messages.AckMessage;
import messages.ControlMessage;
import messages.Message;
import messages.Message.MessageType;
import messages.ControlMessage.ControlType;

public class DroneServer implements Runnable {
	public String PORT_NUMBER;
	public String PASSWORD = "password";
	public String DRONE_ID;
	public UIDrone UI;
	public String VERSION;
	public String RandomNum;
	public DroneDFA droneDFA;
	public DFAResponse droneResponse;
	public DFAResponse nextState;
	public ControlType currentState = ControlType.GROUNDED;
	public int ackmessageId;
	public Message returnmsg;
	public boolean isAuthenticate = false;
	public int messageID = 1000;


	public DroneServer(String p_number, String passward, String drone_id, UIDrone ui) {
		PORT_NUMBER = p_number;
		PASSWORD = passward;
		DRONE_ID = drone_id;
		UI = ui;
		VERSION = "1.1";
		RandomNum = "234";
		droneDFA = new DroneDFA(PASSWORD,VERSION,RandomNum,DRONE_ID);

	}
	
	public String getPASSWORD() {
		return PASSWORD;
	}

	public void setPASSWORD(String pASSWARD) {
		PASSWORD = pASSWARD;
	}

	public String getDRONE_ID() {
		return DRONE_ID;
	}

	public void setDRONE_ID(String dRONE_ID) {
		DRONE_ID = dRONE_ID;
	}

	public String getVERSION() {
		return VERSION;
	}

	public void setVERSION(String vERSION) {
		VERSION = vERSION;
	}

	public String getRandomNum() {
		return RandomNum;
	}

	public void setRandomNum(String randomNum) {
		RandomNum = randomNum;
	}

	/**
	 * read command and call DFA change status and return ACK
	 */

	public void run() {

		try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(PORT_NUMBER));
				Socket clientSocket = serverSocket.accept();

				DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());) {
			int length;
			while ((length = dIn.readInt()) != 0) {
				if (length > 0) {
					byte[] messagebyte = new byte[length];
					dIn.readFully(messagebyte, 0, messagebyte.length); // read the message 
					Message msg;
					
					try {
						msg = Message.fromByteArray(messagebyte);
						if (isAuthenticate){
							
//							droneResponse= droneDFA.authenticate(msg);
							ackmessageId = msg.header.messageID;
							
							nextState = DFAState.getNextState(msg, currentState);
							if (nextState.isErrorFlag()){
								returnmsg = nextState.getMessage();
								UI.display(nextState.getErrorMessage());
								UI.display("error sent");

							} else{
								AckMessage ackMessage = new AckMessage(ackmessageId);
								
								ControlMessage controlMessage = (ControlMessage) nextState.getMessage().body;
								currentState = controlMessage.type;
								returnmsg = new Message(MessageType.ACK,messageID,ackMessage);
								messageID ++;
//								UI.display("ack sent");
							}
							
							testDisplay(returnmsg);

						}else{

							// if not authenticated, the first message must be receiver hello
							droneResponse = droneDFA.authenticate(msg);

							// if error
							if (droneResponse.isErrorFlag()){
								UI.display("3");

								returnmsg = droneResponse.getMessage();
								UI.display(droneResponse.getErrorMessage());
								UI.display("error sent");
							}else{
								UI.display("4");

								// if not error
								
								ControlMessage controlMessage = (ControlMessage) msg.body;
							
								// if receievd message is receiver hello
								if (controlMessage.command == 0x01){
									UI.display("send back Drone Hello");
									returnmsg = droneResponse.getMessage();
									returnmsg.header.messageID = messageID;
									messageID ++;
								}else{
									// if received message is response drone hello
									UI.display("send ack for response drone hello");
									AckMessage ackMessage = new AckMessage(ackmessageId);
									returnmsg = new Message(MessageType.ACK,messageID,ackMessage);
									messageID ++;
									isAuthenticate = true;
									UI.display("Authenticate Done");
								}
								

							}

							
						}

						dOut.writeInt(Message.toByteArray(returnmsg).length);
						dOut.write(Message.toByteArray(returnmsg));
						


					} catch (Exception e) {
						// TODO Auto-generated catch block
						UI.display(e.getMessage());
					}

				}



			}
		} catch (IOException e) {
			UI.display(
					"Exception caught when trying to listen on port " + PORT_NUMBER + " or listening for a connection");
			UI.display(e.getMessage());
		}

	}

	private void displayMsg(Message msg) {
	}

	public void testDisplay(Message msg) {
		for (byte theByte : Message.toByteArray(msg)) {
			UI.display(Integer.toHexString(theByte));
		}
	}
}
