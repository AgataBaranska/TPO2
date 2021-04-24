/**
 *
 *  @author Barańska Agata S19487
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatServer {

	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private String host;
	private int port;
	private List<String> logList;

	public ChatServer(String host, int port) {
		this.host = host;
		this.port = port;
		this.logList = new ArrayList<>();
	}

	// Buffer, do, ktorego beda wczytywane dane z kanalu
	private static final int BSIZE = 256;
	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

	private void handleServiceConnections() {
		while (serverSocketChannel.isOpen()) {
			try {
				selector.select();// Wywolanie blokujace, czeka az selektor powiadomi o gotowosci jakiejs operacji
									// na jakims kanale
				Set<SelectionKey> selectedKeys = selector.selectedKeys();// zbior kluczy opisujacy operacje gotowe do
																			// wykonania i kanaly
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();// przegladanie gotowych kluczy
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();// pobranie klucza, nie ma automatycznego usuwania, musi byc
															// usuniety recznie ze zbioru
					keyIterator.remove();

					if (key.isValid() && key.isAcceptable()) {// jakis klient chce sie polaczyc
						SocketChannel sc = serverSocketChannel.accept();// uzyskanie kanalu do komunikacji z klientem,
																		// accept() nieblokujace, bo juz klient czeka
						sc.configureBlocking(false);// kanal nieblokujacy bo bedzie rejestrowany u selektora

						sc.register(selector, (SelectionKey.OP_READ | SelectionKey.OP_WRITE));// rejestracja kanalu
																								// komunikacji z
																								// klientem za
																								// posrednictwem tego
																								// samego selektora, typ
																								// zdarzenia-dane gotowe
																								// do odczytania przez
																								// serwer

						logList.add(key.attachment() + " logged in");

						continue;

					} else if (key.isValid() && key.isReadable()) {// ktorys z kanalow gotowy do czytania
						SocketChannel socketChannel = (SocketChannel) key.channel();// uzyskanie kanalu, w ktorym sa
																					// dane do odczytania

						// obsluga zdarzenia:

						StringBuilder sb = new StringBuilder();
						int read = 0;
						while ((read = socketChannel.read(bbuf)) > 0) {
							bbuf.flip();
							byte[] bytes = new byte[bbuf.limit()];
							bbuf.get(bytes);
							sb.append(new String(bytes));
							bbuf.clear();
						}
						String msg;
						if (read < 0) {
							msg = key.attachment() + " logged out";
							socketChannel.close();
						} else {
							msg = key.attachment() + ": " + sb.toString();
						}
						logList.add(getDateString() + " " + msg);

						// rozeslij wiadomosc do wszystkich
						broadcast(msg);

						continue;
					}

				}
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	}

	private void broadcast(String msg) {

		ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
		for (SelectionKey key : selector.keys()) {
			if (key.isValid() && key.channel() instanceof SocketChannel) {// dla wszytskich kanalow SocketChannel
				SocketChannel sch = (SocketChannel) key.channel();
				try {
					sch.write(buf);// wpisz zawartosc bufora do kanalu
				} catch (IOException e) {

					e.printStackTrace();
				}
				buf.rewind();// przygotuj buf do kolejnego odczytu
			}
		}

	}

	public void startServer() {
		try {
			serverSocketChannel = ServerSocketChannel.open();// utworzenie kanalu gniazda serwera
			serverSocketChannel.configureBlocking(false);// tryb nieblokujacy
			serverSocketChannel.socket().bind(new InetSocketAddress(host, port));// zwiazanie gniazda serwera z hostem,
																					// // portem
			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);// rejestracja kanalu gniazda serwera u
																			// selektora
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server started");
		handleServiceConnections();

	}

	public void stopServer() {
		try {
			serverSocketChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getServerLog() {
		return logList.toString();
	}

	public String getDateString() {
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
	}

}
