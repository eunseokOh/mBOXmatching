package com.manager.matching;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatchingManager {
	
	private static Map<String, WaitingRoom> roomList; //key : _uuid - �� id
	private Map<String, WaitingUser> allUserList; //key : sessionId - ���� id
	private Map<String, CopyOnWriteArrayList<String>> roomIdList; //key : videoPk
	
	public MatchingManager(){
		roomList = new ConcurrentHashMap<String, WaitingRoom>();
		allUserList = new ConcurrentHashMap<String, WaitingUser>();
		roomIdList = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
	}
	
	public void UserIsReady(String sessionId){
		if(allUserList.containsKey(sessionId)){
			String _uuid = allUserList.get(sessionId).getRoomId();
			
			roomList.get(_uuid).readyUser(sessionId);
		}
	}
	
	public WaitingUser getUserList(String sessionId) {
		
		if(allUserList.containsKey(sessionId)){
			
			return allUserList.get(sessionId);
		}else{
			
			return null;
		}
	}
	
	public  void changeUserStatus(String sessionId, String status){
		roomList.get(allUserList.get(sessionId).getRoomId()).userStatusChange(sessionId,status);
	}
	
	public  void removeUserList(String sessionId) {
		
		String _uuid = allUserList.get(sessionId).getRoomId();
		int userCnt = roomList.get(_uuid).ExitRoom(allUserList.get(sessionId));
		
		if( userCnt < 1 ){
			RemoveRoom(_uuid);
			
		}else{
			
		}
		allUserList.remove(sessionId);
	}

	public  void addUserList(WaitingUser _user) throws IOException {
		
		_user.setRoomId(FindRoom(_user)); 
		allUserList.put(_user.getSession().getId(), _user);
	}

	
	public  String CreateRoom(WaitingUser _user) throws IOException{ //���� �����Ѵ�
		
		UUID uuid = UUID.randomUUID();
		String _uuid = uuid.toString().replace("-", "");
		
		CopyOnWriteArrayList<String> videoPkList = new CopyOnWriteArrayList<String>();
		videoPkList.add(_uuid);
		roomIdList.put(_user.getVideoPk(), videoPkList); //video pk �� uuid List
		
		WaitingRoom room = new WaitingRoom(_user, _uuid);
		roomList.put(_uuid, room);
		System.out.println("create Room");
		
		return _uuid;
	}
	
	public String FindRoom(WaitingUser _user) throws IOException{ //���� ã�Ƽ� ������ �����Ѵ�.
		int max_user = new MatchingManagerApplication().getMAX_CONNECTION_USER_CNT();
		if( roomIdList.containsKey(_user.getVideoPk()) ){
			
			for(int i=0; i< roomIdList.get(_user.getVideoPk()).size();i++){
				String _uuid = roomIdList.get(_user.getVideoPk()).get(i);
				if( roomList.containsKey(_uuid)  //���� �����ϰ�
				 && roomList.get(_uuid).getUserCount() < max_user // 2�� �������ϰ�
				 && !roomList.get(_uuid).isStrated()){ // �������� �ʾ��� ���
					_user.setRoomId(_uuid);
				   roomList.get(_uuid).EnterRoom(_user); // �浵 �ְ� ����� �� ���� ���� ��� ����
				   System.out.println("Enter room");
				   return _uuid;
				}else{
					continue;
				}
			}
			
			 //���� ������ ����� �� á�� ��� ���� �����.
			return CreateRoom(_user);
		}
		
		 //���� �ƿ� ���� ��� ���� �����.
		return CreateRoom(_user);
	}
	
	public  static void RemoveRoom(String _roomId){ //���� �����.
		roomList.remove(_roomId);
		System.out.println("remove Room");
		System.out.println("Room count : "+ getRoomCount());
	}
	
	public static int getRoomCount(){ return roomList.size();} // �� ���� ����

	public void voteVideo(String id, int voteVideoIdx) {
		
		roomList.get(allUserList.get(id).getRoomId()).voteVideo(id,voteVideoIdx);
		
	}
}
