package edu.gatech.parallax;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class ParallaxBlurGUI extends JPanel implements ActionListener {

	private JTextField tAngle, tDistance;
	private JLabel lAngle, lDistance;
	private JButton bLoadImg, bLoadDepth, bRun, bSave;
	private JCheckBox useGPU, linear;
	
	private BufferedImage pix, depth, result;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't change look and feel");
		}

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ParallaxBlurGUI());
		frame.pack();
		frame.setVisible(true);
	}

	private ParallaxBlurGUI() {
		pix = depth = result = null;
		
	}

	private void initComponents() {
		lAngle = new JLabel("Blur Angle: ");
		tAngle = new JTextField("30");
		tAngle.addActionListener(this);
		lDistance = new JLabel("Blur Distance: ");
		tDistance = new JTextField("25");
		tDistance.addActionListener(this);
		
		bLoadImg = new JButton("Load source image");
		bLoadDepth = new JButton("Load depth image");
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
