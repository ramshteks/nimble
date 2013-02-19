package com.ramshteks.nimble.tcp;

import com.ramshteks.nimble.core.*;
import com.ramshteks.nimble.server.IPacketProcessor;
import com.ramshteks.nimble.tcp.events.RawTcpPacketEvent;
import com.ramshteks.nimble.tcp.events.TcpPacketEvent;

import java.io.*;
import java.net.Socket;

/**
 * ...
 *
 * @author Pavel Shirobok (ramshteks@gmail.com)
 */
public class TcpConnection implements EventIO.EventFull {

	private ITcpConnectionEvent connectionEvent;

	private enum IOAction {READ, WRITE}
	private Socket socket;
	private TcpConnectionInfo connectionInfo;
	private IPacketProcessor packetProcessor;
	private OutputStream outputStream;
	private InputStream inputStream;

	private EventStack inputEvents;
	private EventStack outputEvents;

	public TcpConnection(Socket socket, TcpConnectionInfo connectionInfo, IPacketProcessor packetProcessor) throws IOException{

		this.socket = socket;
		this.connectionInfo = connectionInfo;
		this.packetProcessor = packetProcessor;

		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();

		inputEvents = new EventStack(new String[]{TcpPacketEvent.TCP_PACKET_SEND, TcpPacketEvent.LOOP_START});
		outputEvents = new EventStack(new String[]{});
	}

	public void setConnectionEvent(ITcpConnectionEvent connectionEvent){
		this.connectionEvent = connectionEvent;
	}

	@Override
	public void pushEvent(Event event) {
		switch (event.eventType()){
			case Event.LOOP_START:

				readFromStream(inputStream, packetProcessor);
				//read events for receiving packet
				checkAndIO(packetProcessor.fromSocket(), IOAction.READ);
				//read events for sending in socket
				checkAndIO(packetProcessor.toSocket(), IOAction.WRITE);

				break;

			case TcpPacketEvent.TCP_PACKET_SEND:
				//adding bytes to packet processor for send-packing
				addBytesToSend(((TcpPacketEvent)event).bytes());
				break;

		}
	}

	private void readFromStream(InputStream stream, IPacketProcessor processor) {
		int available;
		try {
			if ((available = stream.available()) == 0) {
				return;
			}
		} catch (IOException ioException) {
			//SHIT
			return;
		}


		byte[] raw_input = new byte[available];

		int read;
		try {
			read = stream.read(raw_input);
		} catch (IOException ioException) {
			//SHIT
			return;
		}
		read = 0;

		processor.addToProcessFromSocket(connectionInfo, raw_input);
	}

	private void addBytesToSend(byte[] bytes) {
		packetProcessor.addToProcessToSocket(connectionInfo, bytes);
	}

	private void checkAndIO(EventIO.EventSender eventToSend, IOAction ioAction) {
		if(eventToSend.hasEventToHandle()){
			Event event = eventToSend.nextEvent();

			switch (ioAction) {
				case READ:
					outputEvents.pushEvent(event);
					break;

				case WRITE:
					RawTcpPacketEvent rawTcpPacketEvent = (RawTcpPacketEvent)event;
					if(rawTcpPacketEvent == null){
						//SHIT
						return;
					}
					flushToSocket(rawTcpPacketEvent.bytes());
					break;
			}

		}
	}

	private void flushToSocket(byte[] bytes) {
		//TODO:
		try {
			outputStream.write(bytes);
			outputStream.flush();
		} catch (IOException e) {
			//SHIT
			/*if (connectionEvent != null) {
				connectionEvent.onConnectionClosed(cid);
			}*/
		}
	}

	@Override
	public boolean hasEventToHandle() {
		return outputEvents.hasEventToHandle();
	}

	@Override
	public Event nextEvent() {
		return outputEvents.nextEvent();
	}

	@Override
	public boolean compatibleInput(String eventType) {
		return eventType.equals(TcpPacketEvent.TCP_PACKET_SEND) || eventType.equals(TcpPacketEvent.LOOP_START);
	}

	public void close() {

		try {
			outputStream.close();
		} catch (IOException exp) {
			//SHIT logger.log("Connection:close: outputStream ", exp, Core.LoggerLevel.WARNING);
		}

		try {
			inputStream.close();
		} catch (IOException exp) {
			//SHIT logger.log("Connection:close: inputStream ", exp, Core.LoggerLevel.WARNING);
		}

		try {
			socket.close();
		} catch (IOException exp) {
			//SHIT logger.log("Connection:close: socket ", exp, Core.LoggerLevel.WARNING);
		}

		connectionEvent = null;
		outputStream = null;
		inputStream = null;
		socket = null;
	}
}
