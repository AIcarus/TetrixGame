package com.accela.tetrixgame.gamelogic.uisupport;

import sun.audio.*;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.net.URL;
import java.util.concurrent.*;

/**
 * 
 * 提供声音播放的支持
 * 
 */
public class SoundManager {
	private static final ExecutorService threadPool = Executors
			.newCachedThreadPool();

	// ////////////////////////////////////////////////////////////////

	public static final String BOX_HIT = "audio/boxhit.wav";
	public static final String GAME_OVER = "audio/gameover.wav";
	public static final String START = "audio/start.wav";

	public static boolean isMusicLooping() {
		return isMusicLooping;
	}

	public synchronized static void playSound(final String soundFile) {
		if (null == soundFile) {
			throw new NullPointerException("soundFile should not be null");
		}

		threadPool.submit(new Runnable() {
			public void run() {
				AudioStream as = null;
				try {
					URL url = ResourceLocator.getURL(soundFile);
					if (!musicExists(url)) {
						return;
					}
					as = new AudioStream(url.openStream());
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				assert (as != null);
				AudioPlayer.player.start(as);
			}
		});

	}

	// ////////////////////////////////////////////////////////////////

	private static final String MUSIC = "audio/music/";
	private static final String MUSIC_SUFFIX = ".mid";

	private static final int MUSIC_START_IDX = 1;
	private static final int MUSIC_END_IDX = 8;
	private static AudioClip musicClip = null;
	private static boolean isMusicLooping = false;

	public synchronized static void loopMusic() {
		if (isMusicLooping()) {
			return;
		}

		threadPool.submit(new Runnable() {
			public void run() {
				// 随机选择一首歌曲
				int musicIdx = MUSIC_START_IDX
						+ (int) (Math.random() * (MUSIC_END_IDX
								- MUSIC_START_IDX + 1));
				assert (musicIdx >= MUSIC_START_IDX);
				assert (musicIdx <= MUSIC_END_IDX);

				// 开始播放这首歌曲,TODO AudioClip很容易在内存或cpu紧张的时候后失效，以后要换个实现
				assert (null == musicClip);
				URL url = ResourceLocator.getURL(MUSIC + musicIdx
						+ MUSIC_SUFFIX);
				if (!musicExists(url)) {
					return;
				}
				isMusicLooping = true;
				musicClip = Applet.newAudioClip(url);
				musicClip.loop();
			}
		});
	}

	private static boolean musicExists(URL url) {
		File f = new File(url.getFile());
		if (!f.exists()) {
			return false;
		}
		if (f.isDirectory()) {
			return false;
		}
		return true;
	}

	public synchronized static void stopMusic() {
		if (!isMusicLooping()) {
			return;
		}

		isMusicLooping = false;

		assert (musicClip != null);
		musicClip.stop();
		musicClip = null;
	}

	

	// ////////////////////////////////////////////////////////////

	private static final String APPLAUSE = "audio/applause/";
	private static final String APPLAUSE_SUFFIX = ".wav";

	private static final int APPLAUSE_START_IDX = 1;
	private static final int APPLAUSE_END_IDX = 4;

	public synchronized static void playApplause() {
		threadPool.submit(new Runnable() {
			public void run() {
				// 随机选择一个称赞录音
				int appIdx = APPLAUSE_START_IDX
						+ (int) (Math.random() * (APPLAUSE_END_IDX
								- APPLAUSE_START_IDX + 1));
				assert (appIdx >= APPLAUSE_START_IDX);
				assert (appIdx <= APPLAUSE_END_IDX);

				// 开始播放这个录音
				AudioStream as = null;
				try {
					String file = APPLAUSE + appIdx + APPLAUSE_SUFFIX;
					URL url = ResourceLocator.getURL(file);
					if (!musicExists(url)) {
						return;
					}
					as = new AudioStream(url.openStream());
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				assert (as != null);
				AudioPlayer.player.start(as);
			}
		});
	}

	// ////////////////////////////////////////////////////////////

	private static final String EXPLODE = "audio/explode/";
	private static final String EXPLODE_SUFFIX = ".wav";

	private static final int EXPLODE_START_IDX = 1;
	private static final int EXPLODE_END_IDX = 4;

	public synchronized static void playExplode() {
		threadPool.submit(new Runnable() {
			public void run() {
				// 随机选择一个爆炸的音效
				int expIdx = EXPLODE_START_IDX
						+ (int) (Math.random() * (EXPLODE_END_IDX
								- EXPLODE_START_IDX + 1));
				assert (expIdx >= EXPLODE_START_IDX);
				assert (expIdx <= EXPLODE_END_IDX);

				// 开始播放这个音效
				AudioStream as = null;
				try {
					String file = EXPLODE + expIdx + EXPLODE_SUFFIX;
					URL url = ResourceLocator.getURL(file);
					if (!musicExists(url)) {
						return;
					}
					as = new AudioStream(url.openStream());
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				assert (as != null);
				AudioPlayer.player.start(as);
			}
		});
	}

}
