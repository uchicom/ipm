package com.uchicom.ipm.event;

import com.uchicom.ipm.dto.User;

public class UserChangeEvent {

	private User[] users;
	private Type type;
	private int start;
	private int end;

	public UserChangeEvent(Type type, int start, int end, User... users) {
		this.type = type;
		this.start = start;
		this.end = end;
		this.users = users;
	}

	public User[] getUsers() {
		return users;
	}

	public void setUsers(User[] users) {
		this.users = users;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public static enum Type {
		CHANGED, ADDED, REMOVED, CLEARED;
	}
}
