/**
 *
 *  @author Barańska Agata S19487
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChatClientTask implements Runnable {

	private ChatClient client;
	private List<String> msgs;
	private int wait;

	public ChatClientTask(ChatClient client, List<String> msg, int wait) {
		this.client = client;
		this.msgs = msgs;
		this.wait = wait;
	}

	public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {

		return new ChatClientTask(c, msgs, wait);
	}

	@Override
	public void run() {
		client.login();
		threadSleep();
		for (String msg : msgs) {
			client.send(msg);
			threadSleep();	
		}
		client.logout();
		threadSleep();
	}

	private void threadSleep() {
		if (wait != 0) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public ChatClient getClient() {
		return client;
	}

	public void get() throws InterruptedException, ExecutionException {
		//

	}
}
/*
 * c.login(); if (wait != 0) uśpienie_watku_na wait ms; // ....
 * dla_każdej_wiadomości_z_listy msgs { // ... c.send(wiadomość); if (wait
 * != 0) uśpienie_watku_na wait ms; } c.logout(); if (wait !=
 * 0)uśpienie_watku_na wait ms;
 */