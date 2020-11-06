package com.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.Context;
import com.serial.Serial;
import com.ui.ninePatch.NinePatchPanel;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 10-28-20
 * Description: Dashboard Widget used to display the current state of a connected unit as
 * described over serial communication.
 */
public class StateWidget extends NinePatchPanel {
	private Context context;
	private JFrame infoFrame;
	
	private JLabel apmLabel;
	private JLabel driveLabel;
	private JLabel autoLabel;
	private JLabel flagLabel;
	
	protected static final int BORDER_SIZE = 25;
	
	/**
	 * Class constructor
	 * @param ctx - The application context
	 */
	public StateWidget(Context ctx) {
		super(ctx.theme.panelPatch);
		context = ctx;
		initPanel();
	}

	/**
	 * Construct and place the visual elements of the widget
	 */
	private void initPanel() {
//		Theme theme 		= context.theme;
		Dimension spacer 	= new Dimension(0, 5);
		Dimension labelSize = new Dimension(90, 25);
		
		apmLabel 	= new JLabel("APM - ??");
		driveLabel 	= new JLabel("DRV - ??");
		autoLabel 	= new JLabel("AUT - ??");
		flagLabel 	= new JLabel("FLG - None");
		
		
		ArrayList<JPanel> statePanels = new ArrayList<JPanel>();
		JComponent[] formatList = new JComponent[] {
			apmLabel, driveLabel, autoLabel, flagLabel	
		};
		
		for(JComponent jc : formatList) {
			jc.setAlignmentX(Component.CENTER_ALIGNMENT);
			jc.setMaximumSize(labelSize);
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.setPreferredSize(labelSize);
			panel.setOpaque(false);
			panel.add(jc);
			
			statePanels.add(panel);
			
		}
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(
				BORDER_SIZE, BORDER_SIZE,BORDER_SIZE, BORDER_SIZE));

		for(JPanel panel : statePanels) {
			this.add(panel);
			this.add(Box.createRigidArea(spacer));
		}
	}
	
	/**
	 * Updates the internal state of the widget using byte data received from
	 * serial communications with a device.
	 * @param state - The main state type being updated
	 * @param substate - The main state variation to be updated to
	 */
	public void update(byte state, byte substate) {
		switch(state) {
			case Serial.APM_STATE:
				setAPMState(substate);
				break;
			case Serial.DRIVE_STATE:
				setDriveState(substate);
				break;
			case Serial.AUTO_STATE:
				setAutoState(substate);
				break;
			case Serial.AUTO_FLAGS:
				setFlagState(substate);
				break;
			default:
				System.err.println("Error - Unrecognized State");
		}
	}
	
	/**
	 * Sets the current APM state.
	 * @param substate - The state variation to be set
	 */
	private void setAPMState(byte substate) {
//		System.err.println("StateWidget - Updating APM State");
		
		switch(substate) {
			case Serial.APM_STATE_INIT:
				apmLabel.setText("APM - Init");
				break;
			case Serial.APM_STATE_SELF_TEST:
				apmLabel.setText("APM - Self Test");
				break;
			case Serial.APM_STATE_DRIVE:
				apmLabel.setText("APM - Driving");
				break;
			default:
				apmLabel.setText("APM - Unknown");
				System.err.println("State value: " + substate);
		}
	}
	
	/**
	 * Sets the current Drive state.
	 * @param substate - The state variation to be set
	 */
	private void setDriveState(byte substate) {
//		System.err.println("StateWidget - Updating Drive State");
		
		switch(substate) {
			case Serial.DRIVE_STATE_STOP:
				driveLabel.setText("DRV - Stopped");
				break;
			case Serial.DRIVE_STATE_AUTO:
				driveLabel.setText("DRV - Auto");
				break;
			case Serial.DRIVE_STATE_RADIO:
				driveLabel.setText("DRV - Manual");
				break;
			default:
				driveLabel.setText("DRV - Unknown");
		}
	}
	
	/**
	 * Sets the current Auto state.
	 * @param substate - The state variation to be set
	 */
	private void setAutoState(byte substate) {
//		System.err.println("StateWidget - Updating Auto State");
		
		switch(substate) {
			case Serial.AUTO_STATE_FULL:
				autoLabel.setText("AUT - Full");
				break;
			case Serial.AUTO_STATE_AVOID:
				autoLabel.setText("AUT - Avoid");
				break;
			case Serial.AUTO_STATE_STALLED:
				autoLabel.setText("AUT - Stalled");
				break;
			default:
				autoLabel.setText("AUT - Unknown State");
		}
	}
	
	//TODO - CP - Update flag string AND set an icon indicating severity level
	
	/**
	 * Sets the current state flag if any.
	 * @param substate - The flag type to be set
	 */
	private void setFlagState(byte substate) {
		boolean caution  = ((substate & Serial.AUTO_STATE_FLAGS_CAUTION)  > 0 ) ? true : false;
		boolean approach = ((substate & Serial.AUTO_STATE_FLAGS_APPROACH) > 0 ) ? true : false;
		
//		System.out.println("StateWidget - Updating Flag State");
		
		if(caution && approach) {
			flagLabel.setText("FLG - App. & Caut.");
			//Severity High. Approaching a clear obstable? 
		}
		else if(approach) {
			flagLabel.setText("FLG - Approach");
			//Severity Medium. Approaching an obstacle, slowing down?
		}
		else if(caution) {
			flagLabel.setText("FLG - Caution");
			//Severity Medium. Avoiding an obstacle? 
		}
		else {
			flagLabel.setText("FLG - None");
			//Severity Low. No hazards detected?
		}
	}
	
	/**
	 * Generates an information panel on click describing the warnings, errors,
	 * and details of the current state.
	 */
	private MouseAdapter stateDetailsMouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent me) {
			
			if(infoFrame != null && infoFrame.isVisible()) {
				infoFrame.toFront();
				return;
			}
			else {
				infoFrame = new JFrame("state info");
				JOptionPane.showMessageDialog(infoFrame, "Click Info String Here");
			}
		}
	};
	
}
