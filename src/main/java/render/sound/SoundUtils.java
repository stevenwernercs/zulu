package render.sound;

import javax.sound.sampled.*;
import org.apache.log4j.Logger;

public class SoundUtils {

    private static final Logger log = Logger.getLogger(SoundUtils.class);
    public static float SAMPLE_RATE = 8000f;

    public static void play_tone(int hz, int msecs, double vol)
            throws LineUnavailableException
    {
        log.debug("<play>");
        log.debug("<hz>"+hz+"</hz>");
        log.debug("<msecs>"+msecs+"</msecs>");
        tone(hz, msecs, 1.0);
        log.debug("</play>");
    }

    public static void tone(int hz, int msecs, double vol)
            throws LineUnavailableException
    {
        byte[] buf = new byte[1];
        AudioFormat af =
                new AudioFormat(
                        SAMPLE_RATE, // sampleRate
                        8,           // sampleSizeInBits
                        1,           // channels
                        true,        // signed
                        false);      // bigEndian
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i=0; i < msecs*8; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte)(Math.sin(angle) * 127.0 * vol);
            sdl.write(buf,0,1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    public static void main(String[] args) throws Exception {
        for (double hz = 10; hz < 35000; hz*=1.1) {
            SoundUtils.play_tone((int)hz, 500, 1.0);
            //Thread.sleep(5000);
        }
    }
}
