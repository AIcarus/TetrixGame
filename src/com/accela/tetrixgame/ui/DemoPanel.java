package com.accela.tetrixgame.ui;

import java.awt.*;
import java.io.EOFException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.*;

import com.accela.tetrixgame.conn.support.ObjectStreams.HPObjectInputStream;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyClosedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyOpenedException;
import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;
import com.accela.tetrixgame.net.basic.EngineMirror;
import com.accela.tetrixgame.net.basic.UpdateMessage;
import com.accela.tetrixgame.util.GlobalUpdater;

/**
 * 
 * 用于播放录像的JPanel
 * 
 * //TODO录像功能没有和UI层分离，而且散布在程序逻辑中，以后如果还想要进一步重构，就可以把录像功能独立出来
 * 
 */
public class DemoPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final int MAX_DEMO_NUM = 17;

	private static final int MAX_BUFFERED_MESSAGE_NUM = 30;

	private java.util.List<EngineMirror> mirrors = new LinkedList<EngineMirror>();

	private java.util.List<HPObjectInputStream> demoInputStreams = new LinkedList<HPObjectInputStream>();

	private java.util.List<DemoReaderThread> demoReaders = new LinkedList<DemoReaderThread>();

	private boolean open = false;

	/**
	 * 通过传入的输入流指定从何处读入录像信息。 显示的时候，窗口中的各场游戏从左到有、从 上到下与输入流的一一对应。
	 */
	public DemoPanel(InputStream[] demoIns) {
		if (null == demoIns) {
			throw new NullPointerException("demoIns should not be null");
		}
		if (demoIns.length > MAX_DEMO_NUM) {
			throw new IllegalArgumentException(
					"demoIns.length should not be greater than: "
							+ MAX_DEMO_NUM);
		}
		if (demoIns.length == 0) {
			throw new IllegalArgumentException(
					"demoIns.length should not be zero");
		}
		for (int i = 0; i < demoIns.length; i++) {
			InputStream in = demoIns[i];

			if (null == in) {
				throw new IllegalArgumentException(
						"demoIns should not contains null element");
			}
		}

		initStreams(demoIns);
		initMirrors();
		initPaintLayout();

		this.setBackground(Color.BLACK);
		this.setSize(panelSize);
		this.setPreferredSize(panelSize);

	}

	private void initStreams(InputStream[] demoIns) {
		for (int i = 0; i < demoIns.length; i++) {
			InputStream in = demoIns[i];
			assert (in != null);

			demoInputStreams.add(new HPObjectInputStream(in));
		}
	}

	private void initMirrors() {
		int idx = 0;
		for (HPObjectInputStream in : demoInputStreams) {
			assert (in != null);

			try {
				GameAttributes attr = (GameAttributes) in
						.readObject();
				if (null == attr) {
					throw new IllegalArgumentException();
				}

				assert (Integer.parseInt(attr
						.get(GameAttributes.BLOCK_WIDTH)) == GameAttributes.DEFAULT_BLOCK_WIDTH);
				assert (Integer.parseInt(attr
						.get(GameAttributes.BLOCK_HEIGHT)) == GameAttributes.DEFAULT_BLOCK_HEIGHT);
				assert (Integer
						.parseInt(attr.get(GameAttributes.ROW_NUM)) == GameAttributes.DEFAULT_ROW_NUM);
				assert (Integer.parseInt(attr
						.get(GameAttributes.COLUMN_NUM)) == GameAttributes.DEFAULT_COLUMN_NUM);

				if (0 == idx) {
					mirrors.add(new EngineMirror(attr, false, false));
				} else {
					if (demoInputStreams.size() == 2 && 1 == idx) {
						mirrors.add(new EngineMirror(attr, false, false));
					} else {
						mirrors.add(new EngineMirror(attr, true, true));
					}
				}

			} catch (Exception ex) {
				throw new IllegalArgumentException(
						"inputStream data is in illegal format");
			}

			idx++;
		}
	}

	private Dimension panelSize;

	private int lineNum = 0;

	private Rectangle updateRect;

	private double subDemoScale = 0;

	private Dimension mainDemoGameFieldSize; // 主镜像

	private Dimension subDemoGameFieldSize; // 副镜像

	private boolean isSingleDemo() {
		return 0 == lineNum;
	}

	private void initPaintLayout() {
		panelSize = new Dimension(mirrors.get(0).getGameFieldWidth() * 2,
				mirrors.get(0).getGameFieldHeight());

		lineNum = 0;
		while (lineNum * lineNum < mirrors.size() - 1) {
			lineNum++;
		}

		mainDemoGameFieldSize = new Dimension(mirrors.get(0)
				.getGameFieldWidth(), mirrors.get(0).getGameFieldHeight());

		if (lineNum > 0) {
			subDemoScale = 1.0 / lineNum;
		} else {
			subDemoScale = -1;
		}

		if (lineNum > 0) {
			updateRect = new Rectangle(0, 0, panelSize.width, panelSize.height);
		} else {
			updateRect = new Rectangle(panelSize.width / 4, 0,
					panelSize.width / 2, panelSize.height);
		}

		if (lineNum > 0) {
			subDemoGameFieldSize = new Dimension(
					(int) (mainDemoGameFieldSize.width * subDemoScale),
					(int) (mainDemoGameFieldSize.height * subDemoScale));
		} else {
			subDemoGameFieldSize = null;
		}
	}

	private void initDemoReaders() {
		Iterator<EngineMirror> mirrorItr = mirrors.iterator();
		Iterator<HPObjectInputStream> inItr = demoInputStreams.iterator();

		while (mirrorItr.hasNext()) {
			demoReaders
					.add(new DemoReaderThread(mirrorItr.next(), inItr.next()));
		}

	}

	private boolean checkFieldValid() {
		// 检查null元素
		if (null == mirrors) {
			return false;
		}
		if (null == demoInputStreams) {
			return false;
		}
		if (null == demoReaders) {
			return false;
		}

		for (int i = 0; i < mirrors.size(); i++) {
			if (mirrors.get(i) == null) {
				return false;
			}
		}
		for (int i = 0; i < demoInputStreams.size(); i++) {
			if (demoInputStreams.get(i) == null) {
				return false;
			}
		}
		for (int i = 0; i < demoReaders.size(); i++) {
			if (demoReaders.get(i) == null) {
				return false;
			}
		}

		// 检查长度匹配
		int length = mirrors.size();
		if (demoInputStreams.size() != length) {
			return false;
		}
		if (demoReaders.size() != length) {
			return false;
		}

		// 检查对应位置匹配
		for (int i = 0; i < length; i++) {
			if (demoReaders.get(i).mirror != mirrors.get(i)) {
				return false;
			}
			if (demoReaders.get(i).in != demoInputStreams.get(i)) {
				return false;
			}
		}

		return true;
	}

	public boolean isOpen() {
		return open;
	}

	public void open() {
		if (isOpen()) {
			throw new AlreadyOpenedException();
		}

		open = true;
		try {
			initDemoReaders();
			assert (checkFieldValid());
			initPaintLayout();

			for (DemoReaderThread reader : demoReaders) {
				assert (reader != null);

				reader.start();
			}

			GlobalUpdater.submitTaskUnBlocked(new UpdaterTask());

			// 启动背景音乐
			SoundManager.loopMusic();

		} catch (RuntimeException ex) {
			open = false;
		}
	}

	public void close() {
		if (!isOpen()) {
			throw new AlreadyClosedException();
		}

		open = false;

		for (DemoReaderThread reader : demoReaders) {
			assert (reader != null);

			reader.interrupt();
		}

		// 关闭背景音乐
		SoundManager.stopMusic();
	}

	private Point orign = new Point();

	public static final int GRID_FRAME_WIDTH = 10;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (!isOpen()) {
			return;
		}

		assert (mirrors.size() >= 1);

		if (isSingleDemo()) {
			// 绘制装饰网格
			g.setColor(Color.WHITE);

			g.drawRect(0, 0, panelSize.width / 4, panelSize.height / 2);
			g.drawRect(GRID_FRAME_WIDTH, GRID_FRAME_WIDTH, panelSize.width / 4
					- 2 * GRID_FRAME_WIDTH, panelSize.height / 2 - 2
					* GRID_FRAME_WIDTH);

			g.drawRect(0, panelSize.height / 2, panelSize.width / 4,
					panelSize.height / 2);
			g.drawRect(GRID_FRAME_WIDTH, GRID_FRAME_WIDTH + panelSize.height
					/ 2, panelSize.width / 4 - 2 * GRID_FRAME_WIDTH,
					panelSize.height / 2 - 2 * GRID_FRAME_WIDTH);

			g.drawRect((int) (panelSize.width * 0.75), 0, panelSize.width / 4,
					panelSize.height / 2);
			g.drawRect((int) (panelSize.width * 0.75) + GRID_FRAME_WIDTH,
					GRID_FRAME_WIDTH, panelSize.width / 4 - 2
							* GRID_FRAME_WIDTH, panelSize.height / 2 - 2
							* GRID_FRAME_WIDTH);

			g.drawRect((int) (panelSize.width * 0.75), panelSize.height / 2,
					panelSize.width / 4, panelSize.height / 2);
			g.drawRect((int) (panelSize.width * 0.75) + GRID_FRAME_WIDTH,
					GRID_FRAME_WIDTH + panelSize.height / 2, panelSize.width
							/ 4 - 2 * GRID_FRAME_WIDTH, panelSize.height / 2
							- 2 * GRID_FRAME_WIDTH);

			// 绘制主镜像
			orign.x = panelSize.width / 4;
			orign.y = 0;

			mirrors.get(0).updateAndPaint(g, orign, this, 1.0);
		} else {
			// 画出主镜像
			orign.x = 0;
			orign.y = 0;

			mirrors.get(0).updateAndPaint(g, orign, this, 1.0);

			// 画出副镜像
			int row = 0;
			int column = 0;

			Iterator<EngineMirror> itr = mirrors.iterator();
			itr.next();

			while (itr.hasNext()) {
				EngineMirror mirror = itr.next();

				assert (mirror != null);

				assert (column < lineNum);
				assert (row < lineNum);

				orign.x = panelSize.width / 2 + column
						* subDemoGameFieldSize.width;
				orign.y = row * subDemoGameFieldSize.height;

				mirror.updateAndPaint(g, orign, this, subDemoScale);

				column++;
				if (column >= lineNum) {
					assert (column == lineNum);

					row++;
					column = 0;
				}
			}

		}

	}

	private class DemoReaderThread extends Thread {
		private EngineMirror mirror;

		private HPObjectInputStream in;

		public DemoReaderThread(EngineMirror mirror,
				HPObjectInputStream in) {
			super("DemoPanel - DemoReaderThread");

			if (null == mirror) {
				throw new NullPointerException("mirror should not be null");
			}
			if (null == in) {
				throw new NullPointerException("in should not be null");
			}

			this.mirror = mirror;
			this.in = in;
		}

		public void run() {
			while (isOpen()) {
				try {
					UpdateMessage message = (UpdateMessage) in.readObject();

					mirror.putUpdateMessage(message, MAX_BUFFERED_MESSAGE_NUM);
				} catch (EOFException ex) {
					mirror.enforceGameOver();
					break;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	private class UpdaterTask implements GlobalUpdater.UpdateRunnable {
		public void run() {
			try {
				EventQueue.invokeAndWait(repaintRunnable);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		private class RepaintRunnable implements Runnable {
			public void run() {
				paintImmediately(updateRect);
			}
		}

		private final RepaintRunnable repaintRunnable = new RepaintRunnable();

		@Override
		public boolean isOver() {
			return !isOpen();
		}
	}

}
