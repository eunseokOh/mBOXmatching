package com.manager.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.TextMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WaitingRoom {
	private List<WaitingUser> userList;
	private final String videoPk;
	private final String roomID;
	private boolean isStrated = false;
	private int readyUser;
	private int voteUsers = 0;
	private int voteVideo0 = 0;
	private int voteVideo1 = 0;
	private boolean firstVoted = false;
	private Timer timer;
	public int getReadyUser() {
		return readyUser;
	}

	public synchronized void voteVideo(String id, int no) {

		voteUsers++;

		if (!firstVoted) {
			
			firstVoted = true;
			timer = new Timer();
			TimerTask task = new TimerTask() {
				int stopSec = 15;
				int nowSec = 0;

				@Override
				public void run() {
					nowSec++;
					//System.out.println(nowSec);
					if (stopSec <= nowSec) {
						voteResult();
						if(timer != null){
							timer.cancel();
							timer = null;	
						}
				
					}
				}
			};
			timer.scheduleAtFixedRate(task, 0, 1000);
		}
		if (no == 0) {
			voteVideo0++;
		} else {
			voteVideo1++;
		}
		;

		this.userStatusChange(id, "VOTED");
		if (voteUsers == userList.size()) {
			// System.out.println("vote result send message");
			voteResult();
		}
	}

	public void voteResult() {
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData.put("vote0", voteVideo0);
		resultData.put("vote1", voteVideo1);
		String result = null;
		if (voteVideo0 > voteVideo1) {
			result = "0";
		} else if (voteVideo0 < voteVideo1) {
			result = "1";
		} else {
			int rNum = (int) (Math.random() * 2);

			result = rNum + "";
		}
		resultData.put("result", result);
		Gson gson = new Gson();

		try {
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			SendMessageAllUser(gson.toJsonTree(resultData).toString());

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void userStatusChange(String sessionId, String status) {
		Map<String, Object> data = null;
		Gson gson = null;
		try {
			data = new HashMap<String, Object>();
			data.put("userSid", sessionId);
			data.put("userStatus", status.toUpperCase());
			gson = new Gson();

			SendMessageAllUser(gson.toJsonTree(data).toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void readyUser(String sessionId) {
		readyUser++;

		if (userList.size() % readyUser == 0) {
			try {
				SendMessageAllUser("play");
				System.out.println("play!");
				readyUser = 0;
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	public boolean isStrated() {
		return isStrated;
	}

	public WaitingRoom(WaitingUser _user, String roomdID) throws IOException { // ��
																				// ����
																				// ��
																				// ����
		userList = new CopyOnWriteArrayList<WaitingUser>(); // thread�� ������
															// array list
		userList.add(_user);
		videoPk = _user.getVideoPk();
		roomID = roomdID;
		readyUser = 0;

		Timer timerWaitingRoom = new Timer();
		TimerTask task = new TimerTask() {
			int stopSec = 10;
			int nowSec = 0;

			@Override
			public void run() {
				nowSec++;
				if (stopSec <= nowSec) {

					if (userList.size() > 1) {
						StartRoom();
					} else {
						try {
							Map<String, Object> data = new HashMap<String, Object>();
							data.put("notMatching", "true");
							Gson gson = new Gson();

							SendMessageAllUser(gson.toJson(data).toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					timerWaitingRoom.cancel();

				}

			}
		};

		timerWaitingRoom.scheduleAtFixedRate(task, 0, 1000);
	}

	private synchronized void SendMessageAllUser(String _message) throws IOException {
		for (int i = 0; i < userList.size(); i++) {

			userList.get(i).getSession().sendMessage(new TextMessage(_message));
		}
	}

	private synchronized void SendMessage(String _message, WaitingUser _user) throws IOException {

		_user.getSession().sendMessage(new TextMessage(_message + roomID));

	}

	public void StartRoom() {
		List<HashMap<String, Object>> data;
		HashMap<String, Object> mapData;
		Gson gson = null;

		data = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < userList.size(); i++) {
			mapData = new HashMap<String, Object>();
			// System.out.println(userList.get(i).getUserImg());
			mapData.put("userImg", userList.get(i).getUserImg());
			mapData.put("userName", userList.get(i).getUserPk());
			mapData.put("userSid", userList.get(i).getSession().getId());
			mapData.put("userStatus", "READY");
			data.add(mapData);
		}

		gson = new GsonBuilder().disableHtmlEscaping().create();
		HashMap<String, Object> wrapperData = new HashMap<String, Object>();
		wrapperData.put("enterUserList", data);

		try {
			SendMessageAllUser(gson.toJson(wrapperData));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			isStrated = true;
		}

	}

	public void EnterRoom(WaitingUser _user) throws IOException {
		int max_user = new MatchingManagerApplication().getMAX_CONNECTION_USER_CNT();
		userList.add(_user);

		if (getUserCount() >= max_user) {
			StartRoom();
		}
	}

	public int ExitRoom(WaitingUser _user) {
		userList.remove(_user);
		userStatusChange(_user.getSession().getId(), "EXIT");
		return getUserCount();
	}

	public int getUserCount() {

		return userList.size();
	}

	public String getVideoPk() {
		return videoPk;
	}

}
