package com.map;

import com.Dashboard;
import com.map.coordinateListener;
import com.map.Dot;
import com.serial.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

class WaypointPanel extends JPanel {
	protected static final int MOVE_STEP = 32;
	protected static final String NO_WAYPOINT_MSG = "N / A";
	protected static final String COPY_RIGHT_TEXT = "Map Tiles Courtesy of MapQuest" +
		"\nStreet Data from OpenStreetMap\nPortions Courtesy NASA/JPL-Caltech" +
		"and U.S. Depart. of Agriculture, Farm Service Agency";
	private int selectedWaypoint = 0;
	private MapPanel parent;
	JTextField latitude;
	JTextField longitude;
	JLabel waypointIndexDisplay;

	public WaypointPanel(MapPanel creator) {
		parent = creator;
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		buildPanel();

		this.setPreferredSize(new Dimension(152,320));
	}

	public int getSelectedWaypoint(){
		return selectedWaypoint;
	}

	public void setSelectedWaypoint(int selected){
		selectedWaypoint = selected;
		updateDisplay();
	}

	public void updateDisplay(){
		if (selectedWaypoint > parent.numDot()-1) selectedWaypoint = parent.numDot()-1;
		else if(selectedWaypoint < 0) selectedWaypoint = 0;

		if(parent.numDot() == 0) {
			waypointIndexDisplay.setText(NO_WAYPOINT_MSG);
			latitude.setText("");
			longitude.setText("");
			return;
		}

		String indexField = (selectedWaypoint+1) + " / " + (parent.numDot());
		waypointIndexDisplay.setText(indexField);
		if(selectedWaypoint >= 0 && selectedWaypoint < parent.numDot()) {
			Dot dot = parent.getDot(selectedWaypoint);
			latitude.setText(dot.getLatitude()+"");
			latitude.setForeground(Color.BLACK);
			longitude.setText(dot.getLongitude()+"");
			longitude.setForeground(Color.BLACK);
		}
	}

	private void buildPanel(){
		Insets basic = new Insets(1,1,1,1);
		//add looping button
		JButton looping = new JButton(toggleLooping);
		looping.setAlignmentX(Component.CENTER_ALIGNMENT);
		looping.setMaximumSize(new Dimension(130, 40));
		add(looping);
		//add tile server switcher button
		JPanel tileServer = new JPanel(new FlowLayout());
		JButton tileButton = new JButton(nextTileServer);
		tileButton.setPreferredSize(new Dimension(130,26));
		tileServer.add(tileButton);
		tileServer.setOpaque(false);
		add(tileServer);
		//add navigation layout
		JPanel navigation = new JPanel(new GridLayout(2,3,3,3));
		navigation.setOpaque(false);
		navigation.add(new JLabel());
		navigation.add(new JButton(upAction));
		navigation.add(new JLabel());
		navigation.add(new JButton(leftAction));
		navigation.add(new JButton(downAction));
		navigation.add(new JButton(rightAction));
		add(navigation);
		//add zooming flow layout
		JPanel zoom = new JPanel(new FlowLayout());
		zoom.setOpaque(false);
		zoom.add(new JButton(zoomInAction));
		zoom.add(new JButton(zoomOutAction));
		add(zoom);
		//add spacer
		add(new JSeparator(SwingConstants.HORIZONTAL));
		//add selectedWaypoint flow layout
		JPanel selector = new JPanel(new BorderLayout());
		selector.setOpaque(false);
		waypointIndexDisplay = new JLabel(NO_WAYPOINT_MSG);
		waypointIndexDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		selector.add(new JButton(previousWaypoint), BorderLayout.LINE_START);
		selector.add(waypointIndexDisplay, BorderLayout.CENTER);
		selector.add(new JButton(nextWaypoint), BorderLayout.LINE_END);
		add(selector);
		//add latitude box
		latitude = new JTextField();
		latitude.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		coordinateListener listener = new coordinateListener(latitude, this);
		latitude.getDocument().addDocumentListener(listener);
		latitude.addActionListener(listener);
		add(latitude);
		//add longitude box
		longitude = new JTextField();
		longitude.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		listener = new coordinateListener(longitude, this);
		longitude.getDocument().addDocumentListener(listener);
		longitude.addActionListener(listener);
		add(longitude);

		//add enter button
		JPanel waypointOptions = new JPanel(new FlowLayout());
		waypointOptions.setOpaque(false);
		waypointOptions.add(new JButton(newWaypoint));
		waypointOptions.add(new JButton(interpretLocationAction));
		add(waypointOptions);

		JButton reTarget = new JButton(reTargetRover);
		reTarget.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(reTarget);

		//add save/load flow layout
/*		JPanel saveload = new JPanel(new FlowLayout());
		saveload.setOpaque(false);
		saveload.add(new JButton(saveWaypoints));
		saveload.add(new JButton(loadWaypoints));
		add(saveload);*/

		JTextArea copyRights = new JTextArea();
		Font tmp = copyRights.getFont();
		copyRights.setFont( tmp.deriveFont(7f) );
		copyRights.setOpaque(false);
		copyRights.setLineWrap(true);
		copyRights.setBorder(new EmptyBorder(0,0,0,0));
		copyRights.setText(COPY_RIGHT_TEXT);
		add(copyRights);
	}

	public void interpretLocationEntry(){
		try{
			Double newLatitude = Double.parseDouble(latitude.getText());
			Double newLongitude = Double.parseDouble(longitude.getText());
			Point.Double newPosition = new Point.Double(newLongitude, newLatitude);
			parent.changeDot(selectedWaypoint, newPosition);
			parent.repaint();
		} catch (NumberFormatException e) {}
		updateDisplay();
	}

	public void paint(Graphics gOrig) {
		Graphics2D g = (Graphics2D) gOrig.create();
		try {
			int w = getWidth(), h = getHeight();
			drawBackground(g, w, h);
		} finally {
			g.dispose();
		}
		super.paint(gOrig);
	}

	private static void drawBackground(Graphics2D g, int width, int height) {
		Color color1 = Color.black;
		Color color2 = new Color(0x30, 0x30, 0x30);
		color1 = new Color(0xc0, 0xc0, 0xc0);
		color2 = new Color(0xe0, 0xe0, 0xe0);
		Composite oldComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.75f));
		g.setPaint(new GradientPaint(0, 0, color1, 0, height*2, color2));
		g.fillRoundRect(0, 0, width, height, 4, 4);
		g.setComposite(oldComposite);
	}

	private Action zoomInAction = new AbstractAction() {
		{
			String text = "+";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.zoomInAnimated(new Point(parent.getWidth() / 2, parent.getHeight() / 2));
		}
	};
	private Action zoomOutAction = new AbstractAction() {
		{
			String text = "-";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.zoomOutAnimated(new Point(parent.getWidth() / 2, parent.getHeight() / 2));
		}
	};

	private Action upAction = new AbstractAction() {
		{
			String text = "^";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.translateMapPosition(0, -MOVE_STEP);
			parent.repaint();
		}
	};
	private Action downAction = new AbstractAction() {
		{
			String text = "v";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.translateMapPosition(0, +MOVE_STEP);
			parent.repaint();
		}
	};
	private Action leftAction = new AbstractAction() {
		{
			String text = "<";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.translateMapPosition(-MOVE_STEP, 0);
			parent.repaint();
		}
	};
	private Action rightAction = new AbstractAction() {
		{
			String text = ">";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e) {
			parent.translateMapPosition(+MOVE_STEP, 0);
			parent.repaint();
		}
	};
	private Action nextTileServer = new AbstractAction() {
		String satMSG = "Satellite";
		String mapMSG = "Street";
		boolean state = false;
		{
			String text = satMSG;
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e){
			parent.nextTileServer();
			state = !state;
			putValue(Action.NAME, (state)?satMSG:mapMSG);
			parent.repaint();
		}
	};
	private Action toggleLooping = new AbstractAction(){
		{
			String text = "Looping On";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}

		public void actionPerformed(ActionEvent e){
			parent.setWaypointLooping(!parent.areWaypointsLooped());
			putValue(Action.NAME,
				(parent.areWaypointsLooped())? "Looping Off" : "Looping On");
			parent.repaint();
		}
	};
	private Action previousWaypoint = new AbstractAction(){
		{
			String text = "<";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			setSelectedWaypoint(selectedWaypoint-1);
			updateDisplay();
			parent.repaint();
		}
	};
	private Action nextWaypoint = new AbstractAction(){
		{
			String text = ">";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
			setSelectedWaypoint(selectedWaypoint+1);
			updateDisplay();
			parent.repaint();
		}
	};
	private Action saveWaypoints = new AbstractAction(){

		{
			String text = "Save";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
		}
	};
	private Action loadWaypoints = new AbstractAction(){
		{
			String text = "Load";
			putValue(Action.NAME, text);
			putValue(Action.SHORT_DESCRIPTION, text);
		}
		public void actionPerformed(ActionEvent e){
		}
	};
	private Action interpretLocationAction = new AbstractAction(){
		{
			String text = "Enter";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			interpretLocationEntry();
		}
	};
	private Action reTargetRover = new AbstractAction(){
		{
			String text = "Set Target";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			parent.sendMessage(new Message(Serial.SEND_ROVER_TO_MSG, new Dot(), (byte)selectedWaypoint));
		}
	};
	private Action newWaypoint = new AbstractAction(){
		{
			String text = "New";
			putValue(Action.NAME, text);
		}
		public void actionPerformed(ActionEvent e){
			parent.addDot( new Dot(parent.getDot(selectedWaypoint)), selectedWaypoint );
		}
	};

}

