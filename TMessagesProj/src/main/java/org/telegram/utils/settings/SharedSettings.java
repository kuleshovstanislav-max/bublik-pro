package org.telegram.utils.settings;

import org.telegram.messenger.BuildConfig;
import org.telegram.utils.recorder.round.RoundVideoSession;
import org.telegram.utils.recorder.audio.AudioRecordingSession;

public final class SharedSettings {

    public static final BooleanSetting experimentalSettingsAllowed =
        BooleanSetting.of("experimental_settings_allowed", true);

    public static final BooleanSetting audioRecorderV2Enabled =
        BooleanSetting.of("audio_recorder_v2_enabled", BuildConfig.DEBUG_VERSION);

    public static final EnumSetting<AudioRecordingSession.NoiseSuppression> audioRecorderNoiseSuppression =
        EnumSetting.of("audio_recorder_noise_suppression", AudioRecordingSession.NoiseSuppression.DISABLED);

    public static final BooleanSetting roundVideoCamera2Enabled =
        BooleanSetting.of("round_video_camera2_enabled", true);

    public static final EnumSetting<RoundVideoSession.OutputResolution> roundVideoOutputResolution =
        EnumSetting.of("round_video_output_resolution", RoundVideoSession.OutputResolution.P640);

    public static final EnumSetting<RoundVideoSession.CameraResolution> roundVideoCameraResolution =
        EnumSetting.of("round_video_camera_resolution", RoundVideoSession.CameraResolution.HIGH);

    public static final EnumSetting<RoundVideoSession.FrameRate> roundVideoFrameRate =
        EnumSetting.of("round_video_frame_rate", RoundVideoSession.FrameRate.FPS_30);

    // Bublik Pro: 0 means "Auto": start at 2 Mbps and lower the bitrate while recording so the
    // file stays under Telegram's ~12 MiB round video limit. New key, so everyone starts on Auto.
    public static final int ROUND_VIDEO_BITRATE_AUTO = 0;
    public static final int ROUND_VIDEO_AUTO_START_BITRATE = 2_000_000;
    public static final long ROUND_VIDEO_SIZE_BUDGET_BYTES = 11_800_000L;

    public static final IntSetting roundVideoVideoBitrate =
        IntSetting.of("round_video_bitrate_v2", ROUND_VIDEO_BITRATE_AUTO);

    // Bublik Pro: notifications through our Web Push relay and FCM project (on by default).
    public static final BooleanSetting bublikPushEnabled =
        BooleanSetting.of("bublik_push_enabled", true);

    public static final BooleanSetting roundVideoComposition =
        BooleanSetting.of("round_video_composition", false);

    public static final EnumSetting<RoundVideoSession.CameraFacing> roundVideoLastCamera =
        EnumSetting.of("round_video_last_camera", RoundVideoSession.CameraFacing.FRONT);

    private SharedSettings() {
    }
}
