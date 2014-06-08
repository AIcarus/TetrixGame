package com.accela.tetrixgame.ui;

import javax.swing.JFrame;

import com.accela.tetrixgame.net.hostClient.RunningStation;

/**
 * 
 * 用来显示TetrixRunningStationPanel的JFrame
 * 
 */
public class RunningStationFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private RunningStationPanel panel = null;

	public RunningStationFrame(RunningStation station, String title) {
		panel = new RunningStationPanel(station);

		getContentPane().add(panel);

		if (null == title) {
			title = "";
		}

		this.setTitle(title + " - " + (station.getAllClients().size() + 1)
				+ " Player Tetrix Battle");

		this.setResizable(false);

		// panel.setFocusable(true);
		// panel.requestFocusInWindow();

	}

	@Override
	public void setVisible(boolean visible) {
		pack();

		if (visible) {
			this.setLocationByPlatform(true);

			super.setVisible(visible);

			this.panel.open();
		} else {
			this.panel.close();

			super.setVisible(visible);
		}
	}

}
