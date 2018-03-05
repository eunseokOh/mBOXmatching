package com.manager.matching;

import org.springframework.web.socket.WebSocketSession;

public class WaitingUser {
	
	private String roomId;
	private String userPk;
	private String videoPk;
	private String userImg;
	private WebSocketSession session;
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getUserPk() {
		return userPk;
	}
	public void setUserPk(String userPk) {
		this.userPk = userPk;
	}
	public String getVideoPk() {
		return videoPk;
	}
	public void setVideoPk(String videoPk) {
		this.videoPk = videoPk;
	}
	public String getUserImg() {
		return userImg;
	}
	public void setUserImg(String userImg) {
		this.userImg = userImg;
	}
	public WebSocketSession getSession() {
		return session;
	}
	public void setSession(WebSocketSession session) {
		this.session = session;
	}
	
	@Override
	public String toString() {
		return "roomId:" + roomId + ", userPk:" + userPk + ", videoPk:" + videoPk + ", userImg:" + userImg;
	}
	
	
}
