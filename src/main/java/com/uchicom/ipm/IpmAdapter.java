package com.uchicom.ipm;

import java.io.IOException;

import com.uchicom.ipm.dto.User;
import com.uchicom.ipm.event.ReceiveListener;
import com.uchicom.ipm.type.Mode;
import com.uchicom.ipm.type.Option;

public class IpmAdapter {

	private String username;
	private String hostname;
	private String nickname;
	private String group;
	private boolean absence;

	protected Ipm ipm = new Ipm(10000 + (int) System.currentTimeMillis() % 10000);

	public IpmAdapter(String username, String hostname, String nickname, String group, boolean absence) {
		this.username = username;
		this.hostname = hostname;
		this.nickname = nickname;
		this.group = group;
		this.absence = absence;
		init();
	}

	public String getuser() {
		return username;
	}

	public void setuser(String user) {
		this.username = user;
	}

	public String gethost() {
		return hostname;
	}

	public void sethost(String host) {
		this.hostname = host;
	}

	public String getnickname() {
		return nickname;
	}

	public void setnickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isAbsence() {
		return absence;
	}

	public void setAbsence(boolean absence) {
		this.absence = absence;
	}

	/**
	 * ipmの初期化を実施.
	 */
	private void init() {

		// ここではリスナーの登録を実施。
		try {
			// メッセージ送信、受信以外をあらかじめ自動でやってくれるAdapterかDefaultも用意する。
			// サービスにエントリ（起動時にBroadcast）
			ipm.addListener(Mode.IPMSG_BR_ENTRY, message -> {
				System.out.println(message);
				User user = ipm.addUser(new User.Builder(message).nickName(message.getBody()).build());
				Message.Builder builder = new Message.Builder(user).packetNo(ipm.issuePacketNo())
						.mode(Mode.IPMSG_ANSENTRY).hostName(hostname).userName(this.username).extra(nickname);
				if (absence) {
					builder.option(Option.IPMSG_ABSENCEOPT);
				}
				// .extra() // ニックネームとか
				ipm.sendDelay(builder.build());
			});
			// エントリを認識したことを通知
			ipm.addListener(Mode.IPMSG_ANSENTRY, message -> {
				System.out.println(message);
				ipm.addUser(new User.Builder(message).nickName(message.getBody()).build());
			});
			// メッセージ返却
			ipm.addListener(Mode.IPMSG_RECVMSG, message -> {
				System.out.println("送信確認->" + message);
			});
			// メッセージ受信
			ipm.addListener(Mode.IPMSG_SENDMSG, message -> {
				System.out.println("メッセージ->" + message.getBody());
				if (message.is(Mode.IPMSG_SENDMSG, Option.IPMSG_SENDCHECKOPT)) {
					ipm.send(new Message.Builder(new User.Builder(message).build()).packetNo(ipm.issuePacketNo())
							.mode(Mode.IPMSG_RECVMSG).option(Option.IPMSG_AUTORETOPT).hostName(hostname).userName(username)
							.extra(String.valueOf(message.getPacketNo())).build());
				}

			});
			// 無操作
			ipm.addListener(Mode.IPMSG_NOOPERATION, message -> {
				System.out.println(message);
			});
			// 不在モード変更
			ipm.addListener(Mode.IPMSG_BR_ABSENCE, message -> {
				System.out.println(message);
				ipm.changeUser(new User.Builder(message).build());
			});

			// ホストリスト送出可能メンバの探索
			ipm.addListener(Mode.IPMSG_BR_ISGETLIST, message -> {
				System.out.println(message);
			});
			// ホストリスト送出可能通知
			ipm.addListener(Mode.IPMSG_OKGETLIST, message -> {
				System.out.println(message);
				ipm.send(new Message.Builder(new User.Builder(message).build()).packetNo(ipm.issuePacketNo())
						.mode(Mode.IPMSG_GETLIST).hostName(hostname).userName(username).build());
			});
			// ホストリスト送出要求
			ipm.addListener(Mode.IPMSG_GETLIST, message -> {
				System.out.println(message);
			});
			// ホストリスト送出
			ipm.addListener(Mode.IPMSG_ANSLIST, message -> {
				System.out.println(message);
			});

			// 封書の開封通知
			ipm.addListener(Mode.IPMSG_READMSG, message -> {
				System.out.println(message);

			});
			// 封書破棄通知
			ipm.addListener(Mode.IPMSG_DELMSG, message -> {
				System.out.println(message);
			});
			// 封書の開封確認（8 版から追加）
			ipm.addListener(Mode.IPMSG_ANSREADMSG, message -> {
				System.out.println(message);
			});
			// IPMSGバージョン情報取得
			ipm.addListener(Mode.IPMSG_GETINFO, message -> {
				System.out.println(message);
			});
			// IPMSGバージョン情報応答
			ipm.addListener(Mode.IPMSG_SENDINFO, message -> {
				System.out.println(message);
			});
			// 不在通知文取得
			ipm.addListener(Mode.IPMSG_GETABSENCEINFO, message -> {
				System.out.println(message);
			});
			// 不在通知文応答
			ipm.addListener(Mode.IPMSG_SENDABSENCEINFO, message -> {
				System.out.println(message);
			});
			// サービスから抜ける（終了時にBroadcast）
			ipm.addListener(Mode.IPMSG_BR_EXIT, message -> {
				// ブロードキャストで受信する。
				System.out.println("削除");
				System.out.println(message);
				ipm.removeUser(new User.Builder(message).build());
			});

			ipm.addNetworkListener(broadcast -> {
				ipm.send(new Message.Builder(new User.BroadcastBuilder().build()).packetNo(ipm.issuePacketNo())
						.mode(Mode.IPMSG_BR_ISGETLIST).hostName(hostname).userName(username).build());
			});
			ipm.start();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					ipm.close();
					System.out.println("shutdown hook close!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * エントリー
	 */
	public void entry() {
		Message.Builder builder = new Message.Builder(new User.BroadcastBuilder().build()).packetNo(ipm.issuePacketNo())
				.mode(Mode.IPMSG_BR_ENTRY).hostName(hostname).userName(username).extra(nickname);
		if (absence) {
			builder.option(Option.IPMSG_ABSENCEOPT);
		}
		ipm.send(builder.build());
	}

	/**
	 * setAbsenceしたら自動で通知する？
	 * 
	 * @param absence
	 */
	public void absence(boolean absence) {
		this.absence = absence;
		absence();
	}

	/**
	 * 不在.
	 */
	public void absence() {
		Message.Builder builder = new Message.Builder(new User.BroadcastBuilder().build()).packetNo(ipm.issuePacketNo())
				.mode(Mode.IPMSG_BR_ABSENCE).hostName(hostname).userName(username).extra(nickname);
		if (absence) {
			builder.option(Option.IPMSG_ABSENCEOPT);
		}
		ipm.send(builder.build());
	}

	/**
	 * 退出
	 */
	public void exit() {
		ipm.send(new Message.Builder(new User.BroadcastBuilder().build()).packetNo(ipm.issuePacketNo())
				.mode(Mode.IPMSG_BR_EXIT).hostName(hostname).userName(username).extra(nickname).build());
	}

	public void addListener(Mode mode, ReceiveListener listener) {
		ipm.addListener(mode, listener);
	}

	public void removeListner(Mode mode, ReceiveListener listener) {
		ipm.removeListener(mode, listener);
	}

	public Message createMessage(User user, String text, boolean secret, boolean b) {
		Message.Builder builder = new Message.Builder(user).packetNo(ipm.issuePacketNo()).mode(Mode.IPMSG_SENDMSG)
				.hostName(hostname).userName(this.username).option(Option.IPMSG_SENDCHECKOPT).extra(text);
		if (secret) {
			builder.option(Option.IPMSG_SECRETOPT);
		}
		Message message = builder.build();
		return message;
	}

	/**
	 * メッセージ送信
	 * 
	 * @param message
	 */
	public void send(Message message) {
		ipm.send(message);
	}

	/**
	 * アラート処理
	 * 
	 * @param message
	 * @param runnable
	 */
	public void send(Message message, Runnable runnable) {
		send(message);
		if (runnable != null) {
			new Thread(runnable).start();
		}
	}

	public Long issuePacketNo() {
		return ipm.issuePacketNo();
	}

	public void update() {

		ipm.clearUser();

		entry();
	}

	public void notify(Mode mode, User user, long packetNo) {
		ipm.send(new Message.Builder(user).packetNo(ipm.issuePacketNo()).mode(mode).hostName(hostname).userName(this.username)
				.extra(String.valueOf(packetNo)).build());
	}

	public User getUser(User keyUser) {
		return ipm.getUser(keyUser);
	}

	public Ipm getIpm() {
		return ipm;
	}
}
