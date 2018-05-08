package com.uchicom.ipm.dto;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.uchicom.ipm.Message;
import com.uchicom.ipm.type.Option;

public class User {

	private String name;
	private String host;
	private String nickName;
	private String group;
	private InetAddress inetAddress;
	private int port;
	private boolean absence;
	

	public User(String name, String host, InetAddress inetAddress, int port) {
		this.name = name;
		this.host = host;
		this.inetAddress = inetAddress;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
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

	@Override
	public String toString() {
		StringBuffer strBuff = new StringBuffer(64);
		
		if (absence) {
			strBuff.append("* ");
		}
		if (nickName == null) {
			strBuff.append(name);
		} else {
			strBuff.append(nickName);
		}
		strBuff.append("(");
		strBuff.append(host);
		strBuff.append(")");
		return strBuff.toString();
	}
	
	public static class Builder {

		private String name;
		private String host;
		private InetAddress inetAddress;
		private int port;
		private boolean absence;
		private String nickName;
		public Builder(Message message) {
			name = message.getUserName();
			host = message.getHostName();
			inetAddress = message.getAddress();
			port = message.getPort();
			absence = message.is(Option.IPMSG_ABSENCEOPT);
			nickName = message.getBody();
		}
		
		public Builder nickName(String nickName) {
			this.nickName = nickName;
			return this;
		}
		public User build() {
			User user = new User(name, host, inetAddress, port);
			user.absence = absence;
			user.nickName = nickName;
			return user;
		}
	}
	public static class BroadcastBuilder {

		private String name;
		private String host;
		private InetAddress inetAddress;
		private int port = 2425;
		
		public BroadcastBuilder() {
			try {
				inetAddress = InetAddress.getByName("255.255.255.255");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
		public User build() {
			return new User(name, host, inetAddress, port);
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((inetAddress == null) ? 0 : inetAddress.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nickName == null) ? 0 : nickName.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (inetAddress == null) {
			if (other.inetAddress != null)
				return false;
		} else if (!inetAddress.equals(other.inetAddress))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nickName == null) {
			if (other.nickName != null)
				return false;
		} else if (!nickName.equals(other.nickName))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
}
