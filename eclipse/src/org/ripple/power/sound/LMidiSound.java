package org.ripple.power.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import org.address.collection.ArrayByte;
import org.ripple.power.ui.UIRes;



public class LMidiSound implements MetaEventListener, Sound {

	private static final int MIDI_EOT_MESSAGE = 47;

	private static final int GAIN_CONTROLLER = 7;

	private int volume;

	private Sequencer sequencer;

	private static boolean available;

	private static boolean volumeSupported;

	private static final int UNINITIALIZED = 0;

	private static final int INITIALIZING = 1;

	private static final int INITIALIZED = 2;

	private static int rendererStatus = UNINITIALIZED;
	
	private ArrayByte bytes;

	public LMidiSound() {
		if (rendererStatus == UNINITIALIZED) {
			rendererStatus = INITIALIZING;
			Thread thread = new Thread() {
				public final void run() {
					try {

						Sequencer sequencer = MidiSystem.getSequencer();
						sequencer.open();
						volumeSupported = (sequencer instanceof Synthesizer);

						sequencer.close();
						available = true;
					} catch (Throwable e) {
						available = false;
					}
					rendererStatus = INITIALIZED;
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	public LMidiSound(String fileName) throws IOException {
		bytes = new ArrayByte(UIRes.getStream(fileName),ArrayByte.BIG_ENDIAN);
		if (rendererStatus == UNINITIALIZED) {
			rendererStatus = INITIALIZING;
			Thread thread = new Thread() {
				public final void run() {
					try {

						Sequencer sequencer = MidiSystem.getSequencer();
						sequencer.open();
						volumeSupported = (sequencer instanceof Synthesizer);

						sequencer.close();
						available = true;
					} catch (Throwable e) {
						available = false;
					}
					rendererStatus = INITIALIZED;
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	public boolean isAvailable() {
		if (rendererStatus != INITIALIZED) {
			int i = 0;
			while (rendererStatus != INITIALIZED && i++ < 50) {
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
				}
			}
			if (rendererStatus != INITIALIZED) {
				rendererStatus = INITIALIZED;
				available = false;
			}
		}

		return available;
	}

	public void playSound(String fileName) {
		try {
			playSound(UIRes.getStream(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void playSound() {
		playSound(new ByteArrayInputStream(bytes.getData()));
	}
	
	public void playSound(InputStream in) {
		try {
			System.setProperty("javax.sound.midi.Sequencer",
					"com.sun.media.sound.RealTimeSequencerProvider");
			if (this.sequencer == null) {
				this.sequencer = MidiSystem.getSequencer();
				if (!this.sequencer.isOpen()) {
					this.sequencer.open();
				}
			}

			Sequence seq = MidiSystem.getSequence(in);
			this.sequencer.setSequence(seq);
			this.sequencer.start();
			this.sequencer.addMetaEventListener(this);

			if (this.volume != 1) {
				this.setSoundVolume(this.volume);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void replaySound(URL audiofile) {
		this.sequencer.start();
		this.sequencer.addMetaEventListener(this);
	}

	public void stopSound() {
		this.sequencer.stop();
		this.sequencer.setMicrosecondPosition(0);
		this.sequencer.removeMetaEventListener(this);
	}

	public void meta(MetaMessage msg) {
		if (msg.getType() == MIDI_EOT_MESSAGE) {
			this.sequencer.setMicrosecondPosition(0);
			this.sequencer.removeMetaEventListener(this);
		}
	}

	public void setSoundVolume(int volume) {
		if (this.sequencer == null) {
			return;
		}
		if (volumeSupported) {
			MidiChannel[] channels = ((Synthesizer) this.sequencer)
					.getChannels();
			for (int i = 0; i < channels.length; i++) {
				channels[i].controlChange(GAIN_CONTROLLER, volume);
			}
		}
	}

	public boolean isVolumeSupported() {
		return volumeSupported;
	}

}
