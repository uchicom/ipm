package com.uchicom.ipm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import com.uchicom.ipm.dto.User;
import com.uchicom.ipm.event.NetworkListener;
import com.uchicom.ipm.event.ReceiveListener;
import com.uchicom.ipm.event.UserChangeEvent;
import com.uchicom.ipm.event.UserListener;
import com.uchicom.ipm.type.Mode;

/**
 * 
 * @author Shigeki.Uchiyama
 *
 */
public class Ipm {

	private int port;

	private boolean alive;

	private Map<Mode, List<ReceiveListener>> map = new HashMap<>();

	private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

	private List<NetworkListener> networkListenerList = new ArrayList<>();
	// ユーザマップかリストが必要
	private List<User> userList = new ArrayList<>();

	private List<UserListener> userListenerList = new ArrayList<>();

	private long packetNo = 2;

	public Ipm(int port) {
		this.port = port;
	}

	private DatagramSocket socket;

	private DatagramSocket broadcastSocket;

	/**
	 * 受信スレッド一つに送信スレッド一つ、送信はqueueに詰めてスレッドで繰り返し処理する。
	 */
	public void start() {
		// 待ち受けスタート

		try {
			socket = new DatagramSocket(port);
			alive = true;
			Thread receiveThread = new Thread(() -> {
				byte[] bytes = new byte[4 * 1024];
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
				while (alive) {
					try {
						// ブロードキャストアドレスと、他のネットワークへの直IP
						socket.receive(packet);
						Message message = new Message(new String(packet.getData(), 0, packet.getLength(), "SJIS"),
								packet.getAddress(), packet.getPort());
						List<ReceiveListener> listenerList = map.get(message.getMode());
						if (listenerList != null) {
							SwingUtilities.invokeLater(() -> {
								listenerList.forEach(listener -> {
									listener.receive(message);
								});
							});
						}

						System.out.println("ipm->" + new String(packet.getData(), 0, packet.getLength()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			receiveThread.start();

			Thread sendThread = new Thread(() -> {
				while (alive) {
					try {

						Message message = messageQueue.take();

						byte[] sendBytes = message.create();
						DatagramPacket sendPacket = new DatagramPacket(sendBytes, sendBytes.length,
								new InetSocketAddress(message.getAddress(), message.getPort()));
						socket.send(sendPacket);

					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			sendThread.start();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			broadcastSocket = new DatagramSocket(2425);

			Thread broadcastReceiveThread = new Thread(() -> {
				byte[] bytes = new byte[4 * 1024];
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
				// エラーの場合は待ち秒数を桁であげる
				long wait = 1_000;
				while (true) {
					try {
						// ブロードキャストアドレスと、他のネットワークへの直IP
						broadcastSocket.receive(packet);

						Message message = new Message(new String(packet.getData(), 0, packet.getLength(), "SJIS"),
								packet.getAddress(), packet.getPort());
						List<ReceiveListener> listenerList = map.get(message.getMode());
						if (listenerList != null) {
							listenerList.forEach(listener -> {
								listener.receive(message);
							});
						}
						wait = 1_000;
						System.out.println(new String(packet.getData(), 0, packet.getLength()));

					} catch (IOException e) {
						e.printStackTrace();
						// このエラー処理はここじゃなくてソケット作成時
						networkListenerList.forEach(listener->{
							listener.error(true);
						});
						wait *= 10;
						try {
							Thread.sleep(wait);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			broadcastReceiveThread.start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public long getRandomDelay() {
		Random random = new Random();
		random.setSeed(socket.getLocalSocketAddress().hashCode());
		return new Double(random.nextDouble() * 4000).longValue();
	}

	public void addListener(Mode mode, ReceiveListener listener) {

		if (map.containsKey(mode)) {
			map.get(mode).add(listener);
		} else {
			List<ReceiveListener> list = new ArrayList<>();
			list.add(listener);
			map.put(mode, list);
		}
	}

	public void removeListener(Mode mode, ReceiveListener listener) {
		if (map.containsKey(mode)) {
			map.get(mode).remove(listener);
		}
	}

	public void addUserListener(UserListener userListener) {
		userListenerList.add(userListener);
	}

	public void removeUserListener(UserListener userListener) {
		userListenerList.remove(userListener);
	}
	public void addNetworkListener(NetworkListener networkListener) {
		networkListenerList.add(networkListener);
	}

	public void removeNetorkListener(NetworkListener networkListener) {
		networkListenerList.remove(networkListener);
	}

	public synchronized long issuePacketNo() {
		return packetNo++;
	}

	public void send(Message message) {
		while (!socket.isClosed()) {
			System.out.println("wait...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (socket.isBound()) {
				messageQueue.add(message);
				break;
			}
		}
	}

	public void sendDelay(Message message) {
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(getRandomDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			send(message);
		});
		thread.start();
	}
	
	public int replaceUser(User user) {
		int index = userList.indexOf(user);
		if (index >= 0) {
			userList.remove(user);
			userList.add(index, user);
		}
		return index;
	}

	public User changeUser(User user) {
		if (userList.contains(user)) {
			int index = replaceUser(user);
			for (UserListener listener : userListenerList) {
				listener.changed(new UserChangeEvent(UserChangeEvent.Type.CHANGED, index, index, user));
			}
		}
		return user;
	}

	/**
	 * TODO 変更があった場合対応できない。
	 * @param user
	 * @return
	 */
	public User addUser(User user) {
		if (!userList.contains(user)) {
			userList.add(user);
			int index = userList.indexOf(user);
			for (UserListener listener : userListenerList) {
				listener.changed(new UserChangeEvent(UserChangeEvent.Type.ADDED, index, index, user));
			}
		} else {
			int index = replaceUser(user);
			for (UserListener listener : userListenerList) {
				listener.changed(new UserChangeEvent(UserChangeEvent.Type.CHANGED, index, index, user));
			}
		}
		return user;
	}

	public void clearUser() {
		User[] users = userList.toArray(new User[0]);
		userList.clear();
		for (UserListener listener : userListenerList) {
			listener.changed(new UserChangeEvent(UserChangeEvent.Type.CLEARED, 0, users.length - 1, users));
		}
	}
	public User removeUser(User user) {
		if (userList.contains(user)) {
			int index = userList.indexOf(user);
			userList.remove(user);
			for (UserListener listener : userListenerList) {
				listener.changed(new UserChangeEvent(UserChangeEvent.Type.REMOVED, index, index, user));
			}
		}
		return user;
	}

	public boolean isBroadcast(Mode mode) {
		switch (mode) {
		case IPMSG_BR_ENTRY:
		case IPMSG_BR_EXIT:
		case IPMSG_BR_ABSENCE:
		case IPMSG_BR_ISGETLIST:
			return true;
		default:
			return false;
		}
	}

	public void close() throws IOException {
		alive = false;
		if (socket != null) {
			socket.close();
		}
		if (broadcastSocket != null) {
			broadcastSocket.close();
		}

	}

	public List<User> getUserList() {
		return userList;
	}
}
