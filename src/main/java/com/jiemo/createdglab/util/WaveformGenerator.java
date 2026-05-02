package com.jiemo.createdglab.util;

import java.util.ArrayList;
import java.util.List;

public class WaveformGenerator {

    private static final int BASE_FREQUENCY = 10; // 0x0A

    // Beat waveform - sharp on/off pulses (from DGLab-Craft beat.json)
    private static final String[] BEAT_DATA = {
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464",
        "0A0A0A0A645C5443",
        "0A0A0A0A433A3221",
        "0A0A0A0A00000000",
        "0A0A0A0A00000001",
        "0A0A0A0A02020202"
    };

    // Breath waveform - smooth ramp up and hold (from DGLab-Craft breath.json)
    private static final String[] BREATH_DATA = {
        "0A0A0A0A00000000",
        "0A0A0A0A00050A14",
        "0A0A0A0A14191E28",
        "0A0A0A0A282D323C",
        "0A0A0A0A3C414650",
        "0A0A0A0A64646464",
        "0A0A0A0A64646464",
        "0A0A0A0A64646464",
        "0000000000000000",
        "0000000000000000",
        "0000000000000000"
    };

    // Heartbeat waveform - double-pulse pattern (from DGLab-Craft heartbeat.json)
    private static final String[] HEARTBEAT_DATA = {
        "6E6E6E6E64646464",
        "6E6E6E6E64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A4B4B4B4B",
        "0A0A0A0A4B4D4F53",
        "0A0A0A0A5355585C",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000"
    };

    // RhythmStep waveform - accelerating pulses (from DGLab-Craft rhythm_step.json)
    private static final String[] RHYTHM_STEP_DATA = {
        "0A0A0A0A00000000",
        "0A0A0A0A00050A14",
        "0A0A0A0A14191E28",
        "0A0A0A0A282D323C",
        "0A0A0A0A3C414650",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A00060C19",
        "0A0A0A0A191F2632",
        "0A0A0A0A32383E4B",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A00081021",
        "0A0A0A0A212A3243",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A000C1932",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464"
    };

    // Alarm waveform - sharp pulsing for overload
    private static final String[] ALARM_DATA = {
        "0A0A0A0A64646464",
        "0A0A0A0A64646464",
        "0A0A0A0A50505050",
        "0A0A0A0A28282828",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A64646464",
        "0A0A0A0A64646464",
        "0A0A0A0A50505050",
        "0A0A0A0A28282828",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000",
        "0A0A0A0A00000000"
    };

    /**
     * Get waveform data based on stress level.
     * @param stressPercent 0.0~1.0
     * @param overStressed whether the network is overloaded
     * @return list of 16-char uppercase hex frame strings
     */
    public static List<String> getWaveformForStress(float stressPercent, boolean overStressed) {
        String[] source;
        if (overStressed) {
            source = ALARM_DATA;
        } else if (stressPercent > 0.85f) {
            source = BEAT_DATA;
        } else if (stressPercent > 0.6f) {
            source = RHYTHM_STEP_DATA;
        } else {
            source = BREATH_DATA;
        }

        List<String> result = new ArrayList<>();
        for (String frame : source) {
            result.add(frame.toUpperCase());
        }
        return result;
    }

    /**
     * Build a single 16-char uppercase hex frame.
     * Format: FFFFFFFFSSSSSSSS (4 freq bytes + 4 strength bytes)
     */
    public static String buildHexFrame(int frequency, int strength) {
        int f = Math.min(Math.max(frequency, 10), 100);
        int s = Math.min(Math.max(strength, 0), 100);
        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X", f, f, f, f, s, s, s, s);
    }
}
