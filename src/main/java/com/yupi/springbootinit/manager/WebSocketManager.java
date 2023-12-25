package com.yupi.springbootinit.manager;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.vo.TestWebSocketVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/websocket/{userId}")
@Component
@Slf4j
public class WebSocketManager {

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, WebSocketManager> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String userId = "";


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (session == null) {
            System.out.println("session是null");
        }
        this.session = session;
        System.out.println("连接建立成功调用的方法");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
//
//        if(webSocketMap.containsKey(userId)){
//            webSocketMap.remove(userId);
//            //从set中删除
//            subOnlineCount();
//        }
//        log.info("用户退出:"+userId+",当前在线人数为:" + getOnlineCount());
        System.out.println("连接关闭调用的方法");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:" + userId + ",报文:" + message);
        //可以群发消息
        //消息保存到数据库、redis
//        if(StringUtils.isNotBlank(message)){
//            try {
//                //解析发送的报文
//                JSONObject jsonObject = JSON.parseObject(message);
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        System.out.println("web" + message);
        TestWebSocketVo testWebSocketVo = new TestWebSocketVo();
        testWebSocketVo.setName("hallo");
        String jsonStr = JSONUtil.toJsonStr(testWebSocketVo);
        System.out.println(jsonStr);
        this.session.getBasicRemote().sendText(jsonStr);
//        this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 实现服务器主动推送
     */
    public void sendAllMessage(String message) throws IOException {
//        ConcurrentHashMap.KeySetView<String, WebSocketManager> userIds = webSocketMap.keySet();
//        for (String userId : userIds) {
//            WebSocketManager webSocketServer = webSocketMap.get(userId);
//            webSocketServer.session.getBasicRemote().sendText(message);
//        }
        WebSocketManager webSocketManager = new WebSocketManager();
        webSocketManager.session.getBasicRemote().sendText(message);
        System.out.println(message);
    }

    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        log.info("发送消息到:" + userId + "，报文:" + message);
        if (StringUtils.isNotBlank(userId) && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        } else {
            log.error("用户" + userId + ",不在线！");
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketManager.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketManager.onlineCount--;
    }
}