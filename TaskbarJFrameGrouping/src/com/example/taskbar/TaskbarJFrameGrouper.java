package com.example.taskbar;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

final class TaskbarJFrameGrouper {
	private static final int ROWS = 3;
	private static final int COLUMNS = 4;
	private static final int GAP = 50;
	private final List<GroupableFrame> dummyFrames = new ArrayList<GroupableFrame>();
	private final String[] groups = { "A", "B", "C", "D" };
	private String groupPrefix = String.format("%d", new Random(System.nanoTime()).nextInt());
	private final HashMap<JRadioButton, GroupableFrame> buttonMap = new HashMap<JRadioButton, GroupableFrame>();
	private final HashMap<GroupableFrame, JLabel> labelMap = new HashMap<GroupableFrame, JLabel>();

	private final class GroupableFrame extends JFrame {
		private static final long serialVersionUID = 8203360739282487183L;
		private String customName;

		private GroupableFrame(String customName, String group, int x, int y,
				int width, int height, boolean closeMaster) {
			super(customName + " | " + group);
			this.customName = customName;
			addWindowListener(closeMaster ? null : new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					for (JRadioButton radioButton : TaskbarJFrameGrouper.this.buttonMap
							.keySet()) {
						if (TaskbarJFrameGrouper.this.buttonMap
								.get(radioButton) == GroupableFrame.this) {
							radioButton.setEnabled(false);
						}
					}
					TaskbarJFrameGrouper.this.labelMap.get(GroupableFrame.this)
							.setEnabled(false);
				}
			});

			setLocation(x, y);
			setPreferredSize(new Dimension(width, height));
			pack();
			setDefaultCloseOperation(closeMaster ? JFrame.EXIT_ON_CLOSE
					: JFrame.DISPOSE_ON_CLOSE);
			setGroup(group);
			setVisible(true);
		}

		private String getCustomName() {
			return customName;
		}

		private void setGroup(String group) {
			if (groupingIsPossible()) {
				setTitle(customName + " | " + group);
				long thisWindowHandle = getHWnd(this);
				assignWindowToGroup(thisWindowHandle, groupPrefix + group);
			}
		}
	}

	private static boolean groupingIsPossible() {
		if (System.getProperty("os.name").contains("Windows")) {
			Pattern pattern = Pattern.compile("([0-9]+\\.[0-9]+)");
			Matcher m = pattern.matcher(System.getProperty("os.version"));
			
			if (m.find()) {
				Double windowsVersion = Double.parseDouble(m.group());
				if (windowsVersion >= 6.1) {
					return true;
				}
			}
		}

		return false;
	}

	private final class JRadioButtonItemListener implements ItemListener {
		private JRadioButton attachedRadioButton;

		public JRadioButtonItemListener(JRadioButton attachedRadioButton) {
			super();
			this.attachedRadioButton = attachedRadioButton;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				GroupableFrame frame = buttonMap.get(attachedRadioButton);
				frame.setGroup(attachedRadioButton.getText());
			}
		}
	}

	private GroupableFrame buildMainFrame(String title, String group, int x,
			int y, int width, int height) {
		List<JLabel> labels = new ArrayList<JLabel>();
		for (int i = 0; i < dummyFrames.size(); i++) {
			JLabel label = new JLabel(dummyFrames.get(i).getCustomName());
			labels.add(label);
			labelMap.put(dummyFrames.get(i), label);
		}

		JPanel panel = new JPanel(new GridLayout(dummyFrames.size(),
				1 + group.length()));
		for (int i = 0; i < labels.size(); i++) {
			panel.add(labels.get(i));
			ButtonGroup buttonGroup = new ButtonGroup();
			JRadioButton firstButton = null;
			for (int j = 0; j < groups.length; j++) {
				JRadioButton radioButton = new JRadioButton(groups[j]);
				buttonMap.put(radioButton, dummyFrames.get(i));

				radioButton.addItemListener(new JRadioButtonItemListener(
						radioButton));

				if (j == 0) {
					firstButton = radioButton;
				}
				
				if (!TaskbarJFrameGrouper.groupingIsPossible()) {
					radioButton.setEnabled(false);
				}
				buttonGroup.add(radioButton);
				panel.add(radioButton);
			}
			buttonGroup.setSelected(firstButton.getModel(), true);
		}

		GroupableFrame frame = new GroupableFrame(title, group, GAP, GAP,
				width, height, true);
		frame.add(panel);
		return frame;
	}

	private void populateScreenWithFrames() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();

		int mainFrameWidth = (screenWidth - (COLUMNS + 1) * GAP) / COLUMNS;
		int mainFrameHeight = screenHeight - 2 * GAP;

		int dummyFrameWidth = mainFrameWidth;
		int dummyFrameHeight = (screenHeight - (ROWS + 1) * GAP) / ROWS;
				
		for (int i = 0; i < ROWS; i++) {
			for (int j = 1; j < COLUMNS; j++) {
				GroupableFrame dummyFrame = new GroupableFrame("(" + i + ", "
						+ (j - 1) + ")", groups[0], dummyFrameWidth * j + GAP
						* (j + 1), dummyFrameHeight * i + GAP * (i + 1),
						dummyFrameWidth, dummyFrameHeight, false);
				dummyFrames.add(dummyFrame);
			}
		}

		buildMainFrame("Main frame", groups[0], GAP, GAP, mainFrameWidth,
				mainFrameHeight);
	}

	private native void assignWindowToGroup(long windowHandle, String group);

	
	@SuppressWarnings("deprecation")
	private long getHWnd(Frame f) {
		Class<?> wComponentPeerClass = null;
		if (f.getPeer() != null) {
			try {
				wComponentPeerClass = Class
						.forName("sun.awt.windows.WComponentPeer");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Object componentPeer = wComponentPeerClass.cast(f.getPeer());

			Method[] methods = wComponentPeerClass.getDeclaredMethods();

			for (Method m : methods) {
				if (m.getName().equals("getHWnd")) {
					try {
						return (long) m.invoke(componentPeer);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return 0;
	}

	public static void main(String[] args) {
		new TaskbarJFrameGrouper().populateScreenWithFrames();
	}

	static {
		if (groupingIsPossible()) {
			String dllPathInJar = "group-setter";
			String dllName = null;
			if (System.getProperty("os.arch").equals("x86")) {
				dllName = "WindowGroupSetter_x86";
			} else {
				dllName = "WindowGroupSetter_x64";
			}
			InputStream dllInputStream = TaskbarJFrameGrouper.class
					.getClassLoader().getResourceAsStream(
							dllPathInJar + "/" + dllName + ".dll");

			try {
				File tempFile = File.createTempFile(dllName, ".dll");
				tempFile.deleteOnExit();
				OutputStream out = new FileOutputStream(tempFile);

				byte[] buffer = new byte[4096];
				int length;
				while ((length = dllInputStream.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}

				dllInputStream.close();
				out.close();
				System.load(tempFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
