package org.bigmouth.gpt.xiaozhi.udp;

import com.bxm.warcar.integration.eventbus.EventPark;
import com.bxm.warcar.utils.NamedThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.entity.Session;
import org.bigmouth.gpt.entity.UserFriend;
import org.bigmouth.gpt.entity.UserFriendMediaConfig;
import org.bigmouth.gpt.utils.BatchQueue;
import org.bigmouth.gpt.xiaozhi.audio.AudioPacket;
import org.bigmouth.gpt.xiaozhi.audio.OpusDecoderUtils;
import org.bigmouth.gpt.xiaozhi.audio.OpusEncoderUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiSileroConfig;
import org.bigmouth.gpt.xiaozhi.entity.AudioParams;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.bigmouth.gpt.xiaozhi.entity.MessageType;
import org.bigmouth.gpt.xiaozhi.entity.UdpHello;
import org.bigmouth.gpt.xiaozhi.event.P2pMessageEvent;
import org.bigmouth.gpt.xiaozhi.tts.TtsData;
import org.bigmouth.gpt.xiaozhi.tts.TtsService;
import org.bigmouth.gpt.xiaozhi.vad.SileroVadListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @date 2025/2/25
 */
@Slf4j
@Getter
public class UdpClientContext {

    private final String sessionId;
    private final UdpHello udpHello;
    private final EventPark eventPark;
    private final List<AudioPacket> audioPackets;
    private final OpusEncoderUtils opusEncoderUtils;
    private final OpusDecoderUtils opusDecoderUtils;
    private final BatchQueue<byte[]> audioBuffer;
    private final AtomicReference<State> state;
    private final SileroVadListener sileroVadListener;
    private final AudioResponseSequence audioResponseSequence;
    private final TtsService ttsService;
    private final Session session;
    private final UserFriend userFriend;
    private final UserFriendMediaConfig userFriendMediaConfig;

    /**
     * 收到客户端的udp数据时设置
     */
    private Consumer<byte[]> audioResponseSender;
    private LocalDateTime startVadListeningTime;
    private LocalDateTime stateUpdateTime;

    /**
     * 最后一个收到音频的时间
     */
    private LocalDateTime lastTimeOnReceiveAudio;
    /**
     * 最后一个发送STT的时间
     */
    private LocalDateTime lastTimeOnStt;
    /**
     * 最后一个发送TTS | STOP的时间
     */
    private LocalDateTime lastTimeOnTtsStop;

    /**
     * 存储是否唤醒的标识
     */
    private final AtomicBoolean listenDetect = new AtomicBoolean(false);
    private final AtomicBoolean ttsMakeFinish = new AtomicBoolean(false);
    private final AtomicBoolean audioSendFinish = new AtomicBoolean(false);
    private final BlockingQueue<TtsData> ttsDataBlockingQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor audioSchedulingExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("audio-scheduling"));

    private volatile boolean sayGoodbye = false;


    public UdpClientContext(UdpHello udpHello, EventPark eventPark, List<AudioPacket> audioPackets, OpusEncoderUtils opusEncoderUtils,
                            OpusDecoderUtils opusDecoderUtils, BatchQueue<byte[]> audioBuffer,
                            AtomicReference<State> state, SileroVadListener sileroVadListener,
                            AudioResponseSequence audioResponseSequence,
                            TtsService ttsService, Session session, UserFriend userFriend, UserFriendMediaConfig userFriendMediaConfig) {
        this.sessionId = udpHello.getSessionId();
        this.udpHello = udpHello;
        this.eventPark = eventPark;
        this.audioPackets = audioPackets;
        this.opusDecoderUtils = opusDecoderUtils;
        this.opusEncoderUtils = opusEncoderUtils;
        this.audioBuffer = audioBuffer;
        this.state = state;
        this.sileroVadListener = sileroVadListener;
        this.audioResponseSequence = audioResponseSequence;
        this.ttsService = ttsService;
        this.session = session;
        this.userFriend = userFriend;
        this.userFriendMediaConfig = userFriendMediaConfig;
    }

    public UdpClientContext setSayGoodbye(boolean sayGoodbye) {
        this.sayGoodbye = sayGoodbye;
        return this;
    }

    public UdpClientContext setAudioResponseSender(Consumer<byte[]> audioResponseSender) {
        if (null == this.audioResponseSender) {
            this.audioResponseSender = audioResponseSender;
        }
        return this;
    }

    public UdpClientContext setLastTimeOnReceiveAudio(LocalDateTime lastTimeOnReceiveAudio) {
        this.lastTimeOnReceiveAudio = lastTimeOnReceiveAudio;
        return this;
    }

    public UdpClientContext setLastTimeOnStt(LocalDateTime lastTimeOnStt) {
        this.lastTimeOnStt = lastTimeOnStt;
        return this;
    }

    public UdpClientContext setLastTimeOnTtsStop(LocalDateTime lastTimeOnTtsStop) {
        this.lastTimeOnTtsStop = lastTimeOnTtsStop;
        return this;
    }

    /**
     * 将一个段落添加到队列中
     * @param ttsData tts数据
     */
    public void offerSentence(TtsData ttsData) {
        while (!ttsDataBlockingQueue.offer(ttsData)) {
            sleepInMillis(10);
        }
    }

    public void startAudioSendingSchedule() {
        audioSchedulingExecutor.submit(() -> {
            while (true) {
                if (state.get() == State.Goodbye) {
                    break;
                }
                TtsData ttsData = ttsDataBlockingQueue.poll();
                if (null != ttsData) {
                    if (ttsData.isSendTtsSentenceStart()) {
                        // 发送 TTS | sentence start 请求
                        DataPacket ttsSentenceStart = DataPacket.builder().type(MessageType.TTS.getValue()).state(DataPacket.STATE_SENTENCE_START).text(ttsData.getText()).sessionId(sessionId).build();
                        eventPark.post(new P2pMessageEvent(this, this, ttsSentenceStart));
                    }

                    List<byte[]> ttsDataOpus = ttsData.getOpus();
                    for (byte[] opus : ttsDataOpus) {
                        audioResponseSender.accept(opus);
                        sleepInMillis(opusEncoderUtils.getFrameSizeMs());
                    }
                } else {
                    // 如果TTS已经结束，则退出循环
                    if (ttsMakeFinish.compareAndSet(true, false)) {
                        audioSendFinish.set(true);
                        log.info("[{}] TTS make finished, Exit thread!", sessionId);
                        break;
                    }
                    sleepInMillis(10);
                }
            }
        });
    }

    private void sleepInMillis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public void doTtsMakeFinish() {
        ttsMakeFinish.set(true);
    }

    public void clearVadListeningTimeoutClock() {
        startVadListeningTime = null;
    }

    public boolean isVadListenTimeout(int seconds) {
        if (startVadListeningTime == null) {
            return false;
        }
        boolean after = LocalDateTime.now().isAfter(startVadListeningTime.plusSeconds(seconds));
        if (after) {
            clearVadListeningTimeoutClock();
        }
        return after;
    }

    /**
     * 结束一轮对话后的重置操作
     */
    public void reset() {
        audioPackets.clear();
        audioBuffer.clear();
        state.set(State.Init);
        listenDetect.set(false);
        ttsMakeFinish.set(false);
        audioSendFinish.set(false);
        log.info("[{}] Reset context.", sessionId);
    }

    /**
     * 释放资源
     */
    void destroy() {
        if (!compareAndSet(State.Goodbye, State.Goodbye)) {
            set(State.Goodbye);
            audioPackets.clear();
            audioBuffer.destroy();
            opusEncoderUtils.close();
            opusDecoderUtils.close();
            this.stopVADListener();
            this.audioSchedulingExecutor.shutdownNow();
            if (null != ttsService) {
                ttsService.destroy();
            }
            log.info("[{}] Context has been destroy.", sessionId);
        }
    }

    public void startVADListener() {
        startVadListeningTime = LocalDateTime.now();
        sileroVadListener.init();
        log.info("[{}] Started VAD thread.", sessionId);
    }

    public void stopVADListener() {
        if (!sileroVadListener.isStop()) {
            log.info("[{}] Stop VAD thread.", sessionId);
            sileroVadListener.destroy();
        }
    }

    public void set(State update) {
        stateUpdateTime = LocalDateTime.now();
        state.set(update);
    }

    public boolean compareAndSet(State expect, State update) {
        stateUpdateTime = LocalDateTime.now();
        return state.compareAndSet(expect, update);
    }

    public byte[] getPcmAudioBytes() {
        return mergeByteArrays(audioPackets.stream().map(AudioPacket::getPcm).collect(java.util.stream.Collectors.toList()));
    }

    public static byte[] mergeByteArrays(List<byte[]> byteList) {
        // 计算总长度
        int totalLength = 0;
        for (byte[] byteArray : byteList) {
            totalLength += byteArray.length;
        }

        // 创建目标数组
        byte[] mergedArray = new byte[totalLength];

        // 复制数据
        int currentIndex = 0;
        for (byte[] byteArray : byteList) {
            System.arraycopy(byteArray, 0, mergedArray, currentIndex, byteArray.length);
            currentIndex += byteArray.length;
        }

        return mergedArray;
    }

    public enum State {
        Init,
        ListenStart,
        Listening,
        ListenEnd,
        Goodbye;
    }

    public static class Builder {

        private final UdpHello udpHello;
        private EventPark eventPark;
        private List<AudioPacket> audioPacketList = new ArrayList<>(512);
        private OpusEncoderUtils encoderUtils;
        private OpusDecoderUtils decoderUtils;
        private BatchQueue<byte[]> audioBufferQueue;
        private SileroVadListener sileroVadListener;
        private TtsService ttsService;
        private Session session;
        private UserFriendMediaConfig userFriendMediaConfig;
        private UserFriend userFriend;
        private AtomicReference<State> empty = new AtomicReference<>(State.Init);
        private AudioResponseSequence audioResponseSequence;

        public Builder(UdpHello udpHello) {
            this.udpHello = udpHello;
        }

        public Builder eventPark(EventPark eventPark) {
            this.eventPark = eventPark;
            return this;
        }

        public Builder encoderUtils() {
            AudioParams responseAudioParams = udpHello.getResponse().getAudioParams();
            this.encoderUtils = new OpusEncoderUtils(responseAudioParams.getSampleRate(), responseAudioParams.getChannels(), responseAudioParams.getFrameDuration());
            return this;
        }

        public Builder decoderUtils() {
            AudioParams requestAudioParams = udpHello.getRequest().getAudioParams();
            this.decoderUtils = new OpusDecoderUtils(requestAudioParams.getSampleRate(), requestAudioParams.getChannels(), requestAudioParams.getFrameDuration());
            return this;
        }

        public Builder audioBufferQueue(BatchQueue<byte[]> batchQueue) {
            this.audioBufferQueue = batchQueue;
            return this;
        }

        public Builder sileroVadListener(XiaozhiSileroConfig xiaozhiSileroConfig) {
            AudioParams audioParams = udpHello.getRequest().getAudioParams();

            String modelPath = xiaozhiSileroConfig.getModelPath();
            Integer sampleRate = audioParams.getSampleRate();
            float startThreshold = xiaozhiSileroConfig.getStartThreshold();
            float endThreshold = xiaozhiSileroConfig.getEndThreshold();
            int minSilenceDurationMs = xiaozhiSileroConfig.getMinSilenceDurationMs();
            Integer frameDuration = audioParams.getFrameDuration();

            this.sileroVadListener = new SileroVadListener(modelPath, sampleRate, startThreshold, endThreshold, minSilenceDurationMs, frameDuration);
            return this;
        }

        public Builder ttsService(TtsService ttsService) {
            this.ttsService = ttsService;
            return this;
        }

        public Builder session(Session session) {
            this.session = session;
            return this;
        }

        public Builder userFriendMediaConfig(UserFriendMediaConfig userFriendMediaConfig) {
            this.userFriendMediaConfig = userFriendMediaConfig;
            return this;
        }

        public Builder userFriend(UserFriend userFriend) {
            this.userFriend = userFriend;
            return this;
        }

        public Builder audioResponseSequence(AudioResponseSequence audioResponseSequence) {
            this.audioResponseSequence = audioResponseSequence;
            return this;
        }

        public UdpClientContext build() {
            UdpClientContext context = new UdpClientContext(udpHello, eventPark, audioPacketList, encoderUtils, decoderUtils, audioBufferQueue, empty,
                    sileroVadListener, audioResponseSequence, ttsService, session, userFriend, userFriendMediaConfig);
            Consumer<List<byte[]>> consumer = audioBufferQueue.getConsumer();
            if (consumer instanceof AudioBufferConsumer) {
                ((AudioBufferConsumer) consumer).setContext(context);
            }
            return context;
        }
    }
}






