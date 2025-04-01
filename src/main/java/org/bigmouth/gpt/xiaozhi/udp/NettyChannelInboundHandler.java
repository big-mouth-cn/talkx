package org.bigmouth.gpt.xiaozhi.udp;

import cn.hutool.core.util.HexUtil;
import com.bxm.warcar.integration.eventbus.EventPark;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bigmouth.gpt.utils.BatchQueue;
import org.bigmouth.gpt.xiaozhi.audio.AudioCodec;
import org.bigmouth.gpt.xiaozhi.audio.AudioPacket;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.Udp;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.handler.HelloMessageHandler;
import org.bigmouth.gpt.xiaozhi.handler.ListenMessageHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @date 2025/2/25
 */
@Slf4j
@Configuration
public class NettyChannelInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final HelloMessageHandler helloMessageHandler;
    private final ListenMessageHolder listenMessageHolder;
    private final UdpClientContextBuilder udpClientContextBuilder;
    private final UdpClientContextHolder udpClientContextHolder;
    private final ClientAddressHolder clientAddressHolder;
    private NettyUDPServer nettyUDPServer;

    public NettyChannelInboundHandler(HelloMessageHandler helloMessageHandler, ListenMessageHolder listenMessageHolder, UdpClientContextBuilder udpClientContextBuilder, UdpClientContextHolder udpClientContextHolder, ClientAddressHolder clientAddressHolder) {
        this.helloMessageHandler = helloMessageHandler;
        this.listenMessageHolder = listenMessageHolder;
        this.udpClientContextBuilder = udpClientContextBuilder;
        this.udpClientContextHolder = udpClientContextHolder;
        this.clientAddressHolder = clientAddressHolder;
    }

    public NettyChannelInboundHandler setNettyUDPServer(NettyUDPServer nettyUDPServer) {
        this.nettyUDPServer = nettyUDPServer;
        return this;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) throws Exception {
        try {
            ByteBuf content = packet.content();
            byte[] received = new byte[content.readableBytes()];
            content.readBytes(received);

            InetSocketAddress sender = packet.sender();
            int length = received.length;
            byte[] packetData = new byte[length];
            System.arraycopy(received, 0, packetData, 0, length);

            String packetDataHex = HexUtil.encodeHexStr(packetData);
            // packetDataHex = 01000000a085e3e1553434eb00000000__
            if (StringUtils.length(packetDataHex) <= 32) {
                return;
            }
            String nonce = StringUtils.substring(packetDataHex, 0, 32);
            UdpHello udpHello = helloMessageHandler.getUdpHelloByNonce(nonce);
            if (Objects.isNull(udpHello)) {
                log.warn("Not found UdpSessionCache: {}", nonce);
                return;
            }

            String sessionId = udpHello.getSessionId();
            // 当服务端主动发出 GOODBYE，网络层可能还有数据流进来，防止无效的资源创建，这里做了判断。
            if (udpClientContextHolder.isGoodbye(sessionId)) {
                return;
            }

            // 保存客户端地址
            ClientAddress clientAddress = new ClientAddress().setIp(sender.getAddress().getHostAddress()).setPort(sender.getPort());
            clientAddressHolder.save(sessionId, clientAddress);

            DataPacket listenDataPacket = listenMessageHolder.getListenDataPacket(sessionId);
            if (Objects.isNull(listenDataPacket)) {
                log.warn("Not found listenDataPacket: {}", sessionId);
                return;
            }

            UdpClientContext context = createIfAbsent(udpHello, new Consumer<String>() {
                @Override
                public void accept(String sessionId) {
                    nettyUDPServer.loadClientAddressCache(sessionId);
                }
            });

            // 只有开始监听才生效
            if (listenDataPacket.isStateStart()) {
                DataPacket response = udpHello.getResponse();
                Udp udp = response.getUdp();
                String key = udp.getKey();

                // 将客户端加密的 opus 数据解密
                AudioPacket audioPacket = AudioCodec.decrypt(packetData, key);

                // 将 opus 转成 pcm
                byte[] pcm = context.getOpusDecoderUtils().decodeOpusPacket(audioPacket.getData());
                audioPacket.setPcm(pcm);

                // 暂存每一帧的数据
                context.getAudioPackets().add(audioPacket);

                // 只有当auto模式下才需要 VAD 处理。
                if (listenDataPacket.isModeAuto()) {
                    // 将 pcm 数据存储到队列里，进行 VAD 处理。
                    BatchQueue<byte[]> audioBuffer = context.getAudioBuffer();
                    audioBuffer.asyncAdd(pcm);
                }
            }
        } catch (Exception e) {
            log.error("channelRead0 error: ", e);
        }
    }

    public UdpClientContext createIfAbsent(UdpHello udpHello, @Nullable Consumer<String> ifAbsentHandler) {
        String sessionId = udpHello.getSessionId();
        return udpClientContextBuilder.createIfAbsent(udpHello, ifAbsentHandler)
                .setLastTimeOnReceiveAudio(LocalDateTime.now())
                .setAudioResponseSender(bytes -> nettyUDPServer.sendMessage(bytes, sessionId));
    }
}
