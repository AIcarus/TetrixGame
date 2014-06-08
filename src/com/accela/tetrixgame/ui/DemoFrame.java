package com.accela.tetrixgame.ui;

import java.io.InputStream;

import javax.swing.JFrame;

/**
 * 
 * 用于显示DemoPanel的JFrame
 * 
 */
public class DemoFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private DemoPanel panel = null;

	public DemoFrame(InputStream[] demoIns) {
		panel = new DemoPanel(demoIns);

		getContentPane().add(panel);

		this.setTitle(demoIns.length + " Player Tetrix Demo");

		this.setResizable(false);

		panel.setFocusable(true);
		panel.requestFocusInWindow();

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
