import com.ramshteks.nimble.core.IAccepter;
import com.ramshteks.nimble.core.TcpAccepter;

import java.net.InetAddress;
import java.util.Iterator;

/**
 * ...
 *
 * @author Pavel Shirobok (ramshteks@gmail.com)
 */
public class Main {
	public static void main(String[] args){
		System.out.print("Hello world");

		IAccepter accepter = new TcpAccepter();
		try{
			accepter.startBinding(InetAddress.getLocalHost(), 2305);
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	public static class TestIterable implements Iterable<Integer>, Iterator<Integer>{

		private int i = 0;

		public TestIterable() {

		}

		@Override
		public Iterator<Integer> iterator() {
			i = 0;
			return this;
		}

		@Override
		public boolean hasNext() {
			return i < 10;
		}

		@Override
		public Integer next() {
			return i++;
		}

		@Override
		public void remove() {
		}
	}

}
