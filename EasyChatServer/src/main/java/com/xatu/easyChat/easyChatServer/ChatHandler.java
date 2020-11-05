package com.xatu.easyChat.easyChatServer;


import com.xatu.easyChat.easyChatServer.utils.SpringUtil;
import com.xatu.easyChat.entity.UserChannelRel;
import com.xatu.easyChat.entity.enums.MsgActionEnum;
import com.xatu.easyChat.service.UserService;
import com.xatu.easyChat.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 用于处理消息的handler
 * 由于它的传输数据的载体是frame，这个frame 在netty中，是用于为websocket专门处理文本对象的，frame是消息的载体，此类叫：TextWebSocketFrame
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    //客户端连接channle统一管理
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
       //获取客户端发送的消息
        String text = textWebSocketFrame.text();
        DataContent dataContent = JsonUtils.jsonToPojo(text, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();
        //获取客户端消息执行类型
        Integer action = dataContent.getAction();
        //获取客户端channel
        Channel channel = channelHandlerContext.channel();

        if(action == MsgActionEnum.CONNECT.type) {
            //客户端第一次openSocket 将客户端channel和userId关联
            String senderId = chatMsg.getSenderId();
            UserChannelRel.put(senderId,channel);
        }else if(action == MsgActionEnum.CHAT.type) {
            //获取用户消息保存到数据库并标记为未签收状态
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);
            DataContent receiveDataContent = new DataContent();
            receiveDataContent.setChatMsg(chatMsg);
            //获取接受消息者id
            String receiverId = chatMsg.getReceiverId();
            Channel receiveChannel = UserChannelRel.get(receiverId);
            if (receiveChannel == null) {
                //离线状态
            } else {
                Channel findChannel = channels.find(receiveChannel.id());
                if (findChannel == null) {
                    //离线状态
                } else {
                    //将添加信息id的信息刷写到接收者Channel
                    findChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(receiveDataContent)));
                }
            }


        }else if(action == MsgActionEnum.SIGNED.type) {
            //签收消息dataContent类型针对特定消息进行签收，修改数据库中消息状态为已签收类型


        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        channel.close();
        channels.remove(channel);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端：" +channel.remoteAddress() + "上线");
        channels.add(channel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端：" +channel.remoteAddress() + "下线");
        channels.remove(channel);
    }
}
