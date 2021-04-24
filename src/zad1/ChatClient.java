/**
 *
 *  @author Barańska Agata S19487
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
	private SocketChannel clientSocketChannel;
	private String id;
	private String host;
	private int port;

	List<String> chatView;

	public ChatClient(String host, int port, String id) {
		this.id = id;
		this.host = host;
		this.port = port;
		chatView = new ArrayList<>();
	}

	private static final int BSIZE = 256;
	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

	public void login() {

		try {
			clientSocketChannel = SocketChannel.open();// utworzenie kanalu gniazda klienta
			clientSocketChannel.configureBlocking(false);
			clientSocketChannel.connect(new InetSocketAddress(host, port));

			chatView.add(id + " logged in");

		} catch (IOException e) {
			// wszelkie błędy w interakcji klienta z serwerem (wyjątki exc, np.
			// IOException) powinny być dodawane do chatView klienta jako exc.toString()
			// poprzedzone trzema gwiazdakami
			chatView.add("***" + e.toString());
		}

	}

	public void logout() {

		try {
			clientSocketChannel.close();
		} catch (IOException e) {
			chatView.add("***" + e.toString());
		}
	}

	public String getChatView() {
		return chatView.toString();
	}

	// wysyla do serwera zadanie msg
	public void send(String msg) {
		ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
		while (buf.hasRemaining()) {
			try {
				clientSocketChannel.write(buf);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}
}
