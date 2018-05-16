package com.uchicom.ipm;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import com.uchicom.ipm.dto.User;
import com.uchicom.ipm.type.Mode;
import com.uchicom.ipm.type.Option;

public class Message {

	private Message() {

	}

	public Message(byte[] bytes, InetAddress address, int port) {
		this.address = address;
		this.port = port;
		int index = 0;
		
		String message = null;
		int firstIndex = 0;
		for (;index < bytes.length; index++) {
			if (bytes[index] == 0) {
				if (message == null) {
					try {
						message = new String(bytes, 0, index, "SJIS");
						System.out.println("[" + message + "]");
						firstIndex = index;
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						group = new String(bytes, firstIndex + 1, index - firstIndex - 1, "SJIS");
						System.out.println("[" + group + "]");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		String[] splits = message.split(":");
		version = Integer.parseInt(splits[0]);
		packetNo = Long.parseLong(splits[1]);
		userName = splits[2];
		hostName = splits[3];
		command = Integer.parseInt(splits[4]);

		if (splits.length > 5) {
			body = splits[5];
		}

	}

	private InetAddress address;

	private int port;

	private int version = 1;

	private long packetNo;

	private String userName;

	private String hostName;

	private int command;

	private String body;

	private String group;

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean is(Mode mode, Option... options) {
		if (Mode.of(command) == mode) {
			for (Option option : options) {
				if (!mode.is(option, command)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean is(Mode mode) {
		return Mode.of(command) == mode;
	}

	public boolean is(Option option) {
		return Option.is(command, option);
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public long getPacketNo() {
		return packetNo;
	}

	public void setPacketNo(long packetNo) {
		this.packetNo = packetNo;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Mode getMode() {
		return Mode.of(command);
	}

	public void setMode(Mode mode) {
		command = command & 0xFFFFFF00 | mode.getValue();
	}

	public Option getOption() {
		return Option.of(command);// TODO 複数あるのでoptionsで配列かリストで返す。
	}

	public void clearOption() {
		command &= 0x000000FF; // not and
	}

	public void removeOption(Option option) {
		command &= 0x000000FF;
	}

	public void setOption(Option option) {
		command = command & 0x000000FF | option.getValue();
	}

	public void addOption(Option option) {
		command |= option.getValue();
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public byte[] create() {
		try {
			byte[] bytes = toString().getBytes("SJIS");
			byte[] byte2 = new byte[bytes.length + 1];
			for (int i = 0; i < bytes.length; i++) {
				byte2[i] = bytes[i];
			}
			byte2[bytes.length] = 0;
			return bytes;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	// Ver(1) : Packet番号 : 自User名 : 自Host名 : Command番号 : 追加部

	// 2) 現在のコマンドフォーマットによるメッセージの送信文字列例

	// "1:100:shirouzu:jupiter:32:Hello"

	public String toString() {
		if (body == null) {
			return version + ":" + packetNo + ":" + userName + ":" + hostName + ":" + command + ":";
		} else {
			return version + ":" + packetNo + ":" + userName + ":" + hostName + ":" + command + ":" + body;
		}
	}

	public static class Builder {

		private InetAddress inetAddress;
		private int port;
		private long packetNo;
		private String userName;
		private String hostName;
		private int command;
		private String extra;

		public Builder(User user) {
			this.inetAddress = user.getInetAddress();
			this.port = user.getPort();
		}

		public Builder packetNo(long packetNo) {
			this.packetNo = packetNo;
			return this;
		}

		public Builder hostName(String hostName) {
			this.hostName = hostName;
			return this;
		}

		public Builder userName(String userName) {
			this.userName = userName;
			return this;
		}

		public Builder mode(Mode mode) {
			command = (command & 0xFFFFFF00) | mode.getValue();
			return this;
		}

		public Builder option(Option option) {
			command |= option.getValue();
			return this;
		}

		public Builder extra(String extra) {
			this.extra = extra;
			return this;
		}

		public Message build() {
			Message message = new Message();
			message.address = inetAddress;
			message.port = port;
			message.packetNo = packetNo;
			message.hostName = hostName;
			message.userName = userName;
			message.command = command;
			message.body = extra;
			return message;
		}

	}
}
