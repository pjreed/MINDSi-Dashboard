package com.ui.widgets;

import com.Context;
import com.serial.Serial;
import com.telemetry.TelemetryListener;
import com.util.UtilHelper;

import java.util.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * @author Chris Park @ Infinetix Corp.
 * Date: 06-30-2021
 * Description: Dashbaord widget child class used to display the
 * GPS signal strength of a unit in the field. This strength is determined
 * using a mix of the number of available/visible GPS satellites, and the
 * quality as determined from an HDOP signal.
 */
public class GPSWidget extends UIWidget {
	//Constants
	protected final static int UPDATE_DELAY_MS 		= 5000;
	protected final static int AVERAGE_WINDOW_SIZE 	= 10;
	
	protected final static int MIN_SATS_FOR_LOCK 	= 4;
	
	protected final static double HDOP_MAX_EXCELLENT= 1.0;
	protected final static double HDOP_MAX_GOOD		= 2.0;
	protected final static double HDOP_MAX_FAIR		= 5.0;
	
	//Meter Outer Panel
	protected JPanel meterOuterPanel;
	
	//Meter
	protected ArrayList<JPanel> gpsMeters;
	
	//Meter Update Frequency timer
	protected javax.swing.Timer meterUpdateTimer;
	
	//Satellite Value/State
	protected int satIdx;
	protected int satAvgVal;
	protected double[] satAvgArray;

	//HDOP Value/State
	protected int hdopIdx;
	protected double hdopAvgVal;
	protected double[] hdopAvgArray;
	
	protected GPSStrength currGPSStrength;
	
	/**
	 * Pre-defined strength enum used to track the GPS
	 * signal strength derived from HDOP and Satellite Data. 
	 */
	protected enum GPSStrength {
		UNKOWN		(0),
		POOR		(1),
		FAIR		(2),
		GOOD		(3),
		EXCELLENT 	(4);
		
		private final int strength;
		
		GPSStrength(int strength) {
			this.strength = strength;
		}
		
		public int getValue() {
			return this.strength;
		}
	};
	
	/**
	 * Class constructor
	 * @param ctx - The application context
	 */
	public GPSWidget(Context ctx) {
		super(ctx, "GPS");

		//Init Satellite and HDOP averaging member vars
		satIdx 		 = 0;
		satAvgVal 	 = -1;
		satAvgArray	 = new double[AVERAGE_WINDOW_SIZE];
		
		hdopIdx 	 = 0;
		hdopAvgVal 	 = -1.0;
		hdopAvgArray = new double[AVERAGE_WINDOW_SIZE];
		
		for(int i = 0; i < AVERAGE_WINDOW_SIZE; i++) {
			satAvgArray[i]  = 0.0;
			hdopAvgArray[i] = 0.0;
		}

		//Init current GPS strength state;
		currGPSStrength = GPSStrength.UNKOWN;
		
		//Setup telemetry update listeners
		ctx.telemetry.registerListener(Serial.GPSNUMSAT, new TelemetryListener() {
			/**
			 * Updates and computes a rolling average of the number of satellites.
			 * Satellite data is represented as an integer, so rounding error 
			 * is accounted for in the final calculation.
			 * @param data - The most recent number of satellites
			 */
			public void update(double data) {
				if(satIdx == AVERAGE_WINDOW_SIZE) {
					satIdx = 0;
				}

				satAvgArray[satIdx] = data;
				satAvgVal = (int)(UtilHelper.getInstance().average(
						satAvgArray, AVERAGE_WINDOW_SIZE) + 0.5);
				satIdx++;
			}
		});
		
		ctx.telemetry.registerListener(Serial.GPSHDOP, new TelemetryListener() {
			/**
			 * Updates and computes a rolling average of HDOP values.
			 * @param data - The most recent HDOP value
			 */
			public void update(double data) {				
				if(hdopIdx == AVERAGE_WINDOW_SIZE) {
					hdopIdx = 0;
				}
				
				hdopAvgArray[hdopIdx] = data;
				hdopAvgVal = UtilHelper.getInstance().average(
						hdopAvgArray, AVERAGE_WINDOW_SIZE);
				hdopIdx++;
			}
		});
		
		//Build Meter Widget
		meterOuterPanel = new JPanel();
		meterOuterPanel.setMinimumSize(new Dimension(100, 60));
		meterOuterPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 0, 2, 0);
		constraints.anchor = GridBagConstraints.CENTER;
		
		gpsMeters = buildMeterSet();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		meterOuterPanel.add(gpsMeters.get(GPSStrength.UNKOWN.getValue()), constraints);
		
		this.add(meterOuterPanel);
		
		//Kick off the visual update timer.
		meterUpdateTimer = new javax.swing.Timer(UPDATE_DELAY_MS, meterUpdateAction);
		meterUpdateTimer.start();
	}
	
	/**
	 * Constructs a complete set of meter graphics to be used for a GPS signal
	 * strength meter.
	 * @return
	 */
	protected ArrayList<JPanel> buildMeterSet() {
		ArrayList<JPanel> meterSet;
		JPanel panel;
		
		meterSet = new ArrayList<JPanel>();
		
		//No Signal (Unknown)
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Poor Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Fair Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Good Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterSpacer)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);
		
		//Excellent Signal
		panel = new JPanel();
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterRed)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterYellow)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.add(new JLabel(new ImageIcon(context.theme.verticalMeterGreen)));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		meterSet.add(panel);

		return meterSet;
	}
	
	/**
	 * Periodic update responsible for updating the GPS widget visual meter.
	 * This is fired by a predetermined timer value. See
	 * UPDATE_DELAY_MS for interrupt period.
	 */
	ActionListener meterUpdateAction = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			determineSignalStrength();
		}
	};
	
	/**
	 * Determines the current GPS signal strength based on the number 
	 * of satellites visible and HDOP values. 
	 * 
	 * The Algorithm: At least 4 GPS satellites are needed to get a reliable
	 * location fix on a position. If the number of satellites does not at least
	 * equal this (MIN_SATS_FOR_LOCK) then the signal is considered poor and HDOP
	 * values are not considered. If the minimum satellite requirement is met then
	 * HDOP values are evaluated to determine the strength of the signal. (See
	 * HDOP_MAX_[X] constants for range details)
	 * 
	 *  Once this is calculation is performed, the visual meter 
	 * representation is updated. 
	 */
	protected void determineSignalStrength() {
		
		//Determine the current GPS signal strength
		if(satAvgVal < MIN_SATS_FOR_LOCK) {
			currGPSStrength = GPSStrength.POOR;
		}
		else {
			if(hdopAvgVal < HDOP_MAX_EXCELLENT) {
				currGPSStrength = GPSStrength.EXCELLENT;
			}
			else if ((hdopAvgVal >= HDOP_MAX_EXCELLENT) 
				  && (hdopAvgVal <= HDOP_MAX_GOOD)) {
				currGPSStrength = GPSStrength.GOOD;
			}
			else if ((hdopAvgVal > HDOP_MAX_GOOD) 
				  && (hdopAvgVal <= HDOP_MAX_FAIR )) {
				currGPSStrength = GPSStrength.FAIR;
			}
			else if (hdopAvgVal > HDOP_MAX_FAIR) {
				currGPSStrength = GPSStrength.POOR;
			}
			else {
				currGPSStrength = GPSStrength.UNKOWN;
			}
		}
		
		//Update the visual meters accordingly
		updateMeter();
	}
	
	/**
	 * Updates the graphical representation of GPS signal strength for the
	 * widget.
	 */
	protected void updateMeter() {
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridy = 0;
		constraints.gridx = 0;
		
		meterOuterPanel.removeAll();
		meterOuterPanel.add(gpsMeters.get(currGPSStrength.getValue()), constraints);
		
	}
}