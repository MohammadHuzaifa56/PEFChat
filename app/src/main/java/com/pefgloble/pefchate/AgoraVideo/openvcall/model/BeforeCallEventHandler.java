package com.pefgloble.pefchate.AgoraVideo.openvcall.model;

import com.pefgloble.pefchate.AgoraVideo.openvcall.model.AGEventHandler;

import io.agora.rtc.IRtcEngineEventHandler;

public interface BeforeCallEventHandler extends AGEventHandler {
    void onLastmileQuality(int quality);

    void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result);
}
