import com.ramshteks.nimble.core.Event;
import com.ramshteks.nimble.core.EventIO;
import com.ramshteks.nimble.core.Nimble;
import com.ramshteks.nimble.server.IPacketProcessor;
import com.ramshteks.nimble.server.IPacketProcessorFactory;
import com.ramshteks.nimble.server.ServerUtils;
import com.ramshteks.nimble.tcp.TcpConnectionInfo;
import com.ramshteks.nimble.tcp.TcpConnectionsStack;
import com.ramshteks.nimble.tcp.TcpReceptor;

import java.net.InetAddress;

/**
 * ...
 *
 * @author Pavel Shirobok (ramshteks@gmail.com)
 */
public class Main {
	public static void main(String[] args) {

		Nimble nimble = new Nimble();

		IPacketProcessorFactory packetProcessorFactory = new IPacketProcessorFactory() {
			@Override
			public IPacketProcessor createNewInstance(TcpConnectionInfo connectionInfo) {
				return null;
			}
		};

		ServerUtils.IDGenerator idGenerator = new ServerUtils.IDGenerator(0, 100000);
		TcpConnectionsStack connStack = new TcpConnectionsStack(nimble, idGenerator, packetProcessorFactory);
		TcpReceptor tcpReceptor = new TcpReceptor(connStack);

		try {
			tcpReceptor.startBinding(InetAddress.getLocalHost(), 4000);
		} catch (Exception e) {
		}

		nimble.addFullEventPlugin(tcpReceptor);

		//nimble.start();




	}

	public static class TestPlugin implements EventIO.EventFull{
		private int counter = 0;
		private long last = System.currentTimeMillis();
		private String s;

		public TestPlugin(String s) {

			this.s = s;
		}

		@Override
		public void pushEvent(Event event) {
			if("Test".equals(event.eventType())){
				//if(counter % 1000 == 0){
					System.out.println(s + " " + (System.currentTimeMillis() - last));
					last = System.currentTimeMillis();
					counter= 0;
				//}
				counter++;
			}
		}

		@Override
		public boolean compatibleInput(String eventType) {
			return true;
		}

		@Override
		public boolean hasEventToHandle() {
			return true;
		}
		private Event event = new Event("Test");
		@Override
		public Event nextEvent() {
			return event;
		}
	}

}
