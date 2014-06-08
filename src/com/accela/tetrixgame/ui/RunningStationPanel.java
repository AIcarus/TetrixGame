package com.accela.tetrixgame.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.net.hostClient.RunningStation;
import com.accela.tetrixgame.util.GlobalUpdater;

/**
 * 想要即高效又美观地显示TetrixRunningStation，即整场游戏，就需要用到这个 精心制作的JPanel。
 */
public class RunningStationPanel extends JPanel implements KeyListener {
	private static final long serialVersionUID = 1L;

	private RunningStation station;

	public static final int MAX_PLAYER_NUM = 17;

	public RunningStationPanel(RunningStation station) {
		if (null == station) {
			throw new NullPointerException("station should not be null");
		}
		if (station.isOpen()) {
			throw new IllegalArgumentException(
					"station should have not be opened");
		}
		if (station.getAllClients().size() > MAX_PLAYER_NUM - 1) {
			throw new IllegalArgumentException(
					"station should have not greater than " + MAX_PLAYER_NUM
							+ " players");
		}

		this.station = station;
		this.addKeyListener(this); // 由TetrixRunningStationFrame代办了

		this.setBackground(Color.BLACK);

		initPaintLayout();
		this.setSize(panelSize);
		this.setPreferredSize(panelSize);

		this.setFocusable(true);
	}

	private Dimension panelSize;

	private int lineNum = 0;

	private double mirrorScale = 0;

	private Dimension engineGameFieldSize;

	private Dimension mirrorGameFieldSize;

	private boolean isSinglePlayer() {
		return 0 == lineNum;
	}

	private void initPaintLayout() {
		java.util.List<ClientID> clientList = station.getAllClients();
		assert (clientList.size() <= MAX_PLAYER_NUM - 1);

		panelSize = new Dimension(station.getGameFieldWidth() * 2,
				station.getGameFieldHeight());

		lineNum = 0;
		while (lineNum * lineNum < clientList.size()) {
			lineNum++;
		}

		engineGameFieldSize = new Dimension(station.getGameFieldWidth(),
				station.getGameFieldHeight());

		if (lineNum > 0) {
			mirrorScale = 1.0 / lineNum;
		} else {
			mirrorScale = -1;
		}

		if (lineNum > 0) {
			new Rectangle(0, 0, panelSize.width, panelSize.height);
		} else {
			new Rectangle(panelSize.width / 4, 0,
					panelSize.width / 2, panelSize.height);
		}

		if (lineNum > 0) {
			mirrorGameFieldSize = new Dimension(
					(int) (engineGameFieldSize.width * mirrorScale),
					(int) (engineGameFieldSize.height * mirrorScale));
		} else {
			mirrorGameFieldSize = null;
		}
	}

	public boolean isOpen() {
		return station.isOpen();
	}

	public void open() {
		try {
			station.open();
		} catch (FailedToOpenException ex) {
			ex.printStackTrace();
		}

		GlobalUpdater.submitTaskUnBlocked(new UpdateTask());
	}

	public void close() {
		try {
			station.close();
		} catch (FailedToCloseException ex) {
			ex.printStackTrace();
		}
	}

	private Point orign = new Point();

	public static final int GRID_FRAME_WIDTH = 10;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (isSinglePlayer()) {
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

			// 绘制引擎
			orign.x = panelSize.width / 4;
			orign.y = 0;

			station.updateAndPaintEngine(g, orign, this, 1.0);
		} else {
			// 画出引擎
			orign.x = 0;
			orign.y = 0;

			station.updateAndPaintEngine(g, orign, this, 1.0);

			// 画出镜像
			int row = 0;
			int column = 0;
			for (ClientID client : station.getAllClients()) {
				assert (client != null);
				assert (column < lineNum);
				assert (row < lineNum);

				orign.x = panelSize.width / 2 + column
						* mirrorGameFieldSize.width;
				orign.y = row * mirrorGameFieldSize.height;

				station.updateAndPaintMirror(client, g, orign, this,
						mirrorScale);

				column++;
				if (column >= lineNum) {
					assert (column == lineNum);

					row++;
					column = 0;
				}

			}
		}

	}

	private class UpdateTask implements GlobalUpdater.UpdateRunnable {
		public void run() {
			repaint();
			// try
			// {
			// EventQueue.invokeAndWait(repaintRunnable);
			// } catch (InvocationTargetException ex)
			// {
			// ex.printStackTrace();
			// } catch (InterruptedException ex)
			// {
			// ex.printStackTrace();
			// }
		}

		@Override
		public boolean isOver() {
			return !isOpen();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Common.LOG.info("Key pressed here: "+e);
		if (isOpen()) {
			station.keyPressed(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (isOpen()) {
			station.keyReleased(e.getKeyCode());
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
