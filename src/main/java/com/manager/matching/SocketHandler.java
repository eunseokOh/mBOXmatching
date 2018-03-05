package com.manager.matching;



import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;


@Component
public class SocketHandler extends TextWebSocketHandler{
	
	
	MatchingManager matchingManager;
	
	
	public SocketHandler(){
		matchingManager = new MatchingManager();
	}
	
	@Override //connection�� �������� �޼����� ���� ��
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		if(message.getPayload().equals("ready")){ //�濡 ������ ����
			matchingManager.UserIsReady(session.getId());
		}else{
			Gson gson = new Gson();
			Map<String, Object> map = new HashMap<String, Object>();
			map = (Map<String, Object>)gson.fromJson(message.getPayload(), map.getClass());
			
			if(map.get("videoPk") != null){
				WaitingUser _user = new Gson().fromJson(message.getPayload(), WaitingUser.class);	
				_user.setSession(session);
				matchingManager.addUserList(_user);
			}else if(map.get("voteVideoIdx") != null){
				matchingManager.voteVideo(session.getId(), Integer.parseInt(map.get("voteVideoIdx").toString()));
			}else if(map.get("userStatus") != null){
				matchingManager.changeUserStatus(session.getId(), map.get("userStatus").toString());
			}
		
			//System.out.println(_user.toString());
		}
	};
	
	@Override //WebSocket ���� ���� ��
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		System.out.println("Connection : " + session.getId());
	}
	
	@Override //WebSocket ���� ���� ��
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		if(matchingManager.getUserList(session.getId()) != null){
			matchingManager.removeUserList(session.getId());
			
			System.out.println("Connection Close : " + session.getId());
			
			
		}else{
			
			System.out.println("No find room after connection - Close : " + session.getId());
		}
		
	}
	

}
