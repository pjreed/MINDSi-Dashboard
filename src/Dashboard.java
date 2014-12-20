package com;

import com.map.MapPanel;
import com.serial.SerialSender;
import com.serial.SerialParser;
import com.serial.Serial;
import com.ui.*;
import com.Logger;
import com.map.WaypointList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.geom.AffineTransform;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Dashboard implements Runnable {
  JPanel serialPanel;
  JButton refreshButton;
  JButton connectButton;
  JComboBox dropDown;
  RotatePanel sideGauge;
  RotatePanel topGauge;
  RotatePanel frontGauge;
  DataLabel latitude;
  DataLabel longitude;
  DataLabel heading;
  DataLabel speed;
  DataLabel pitch;
  DataLabel roll;
  DataLabel voltage;
  Frame loading;
  static final int[] dataBorderSize = {15,18,46,18};//top,left,bottom,right
  JFrame f;
  Context context;
  MapPanel mapPanel;

  @Override
  public void run() {
    try{
      BufferedImage logo = ImageIO.read(new File("./data/startup-logo.png"));
      loading = new Frame("MINDS-i Loading Box");
      loading.setUndecorated(true);
      loading.setBackground(new Color(0,0,0,0));
      loading.add(new JLabel(new ImageIcon(logo)));
      loading.pack();
      loading.setSize(540,216);
      loading.setLocationRelativeTo(null);
      loading.setVisible(true);

      context = new Context();
      Theme theme = new Theme("./data/");
      context.give(this,
                   new AlertPanel(theme.ocr),
                   new SerialSender(context),
                   new SerialParser(context),
                   new WaypointList(context),
                   new Logger(context),
                   null, //serialPort
                   theme);
      InitUI();
    } catch (IOException e) {
      DisplayError((Exception)e);
    }
  }

  private void InitUI(){
    f = new JFrame("MINDS-i Dashboard");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(false);
    f.setIconImage(context.theme.roverTop);
    f.setTitle("MINDS-i dashboard");

    Action refreshAction = new AbstractAction(){
      {
        String text = "Refresh";
        putValue(Action.SHORT_DESCRIPTION, text);
        putValue(Action.SMALL_ICON, new ImageIcon(context.theme.refreshImage));
      }
      public void actionPerformed(ActionEvent e) {
        dropDown.removeAllItems();
        AddSerialList(dropDown);
        serialPanel.updateUI();
      }
    };
    Action connectAction = new AbstractAction(){
      {
        String text = "Connect";
        putValue(Action.NAME, text);
        putValue(Action.SHORT_DESCRIPTION, text);
      }
      public void actionPerformed(ActionEvent e){
        if (context.connected == false){
          if(connectSerial())
            putValue(Action.NAME, "Disconnect");
        }
        else {
          if(disconnectSerial())
            putValue(Action.NAME, "Connect");
        }
      }
    };

    serialPanel = new JPanel(new FlowLayout());
    refreshButton = new JButton(refreshAction);
    connectButton = new JButton(connectAction);
    dropDown = new JComboBox();
    AddSerialList(dropDown);
    refreshButton.setToolTipText("Refresh");
    connectButton.setToolTipText("Attempt connection");
    serialPanel.add(refreshButton);
    serialPanel.add(dropDown);
    serialPanel.add(connectButton);
    serialPanel.setOpaque(false);

    mapPanel = new MapPanel(  context,
                              new Point(628,1211),
                              4,
                              serialPanel,
                              makeDashPanel(),
                              context.alert);
    mapPanel.setVgap(-45);
    serialPanel.setOpaque(false);

    f.add(mapPanel);
    f.pack();
    loading.dispose();
    f.setVisible(true);
    f.setSize(800, 650);
  }

  private void AddSerialList(JComboBox box){
    String[] portNames = SerialPortList.getPortNames();
    for(int i = 0; i < portNames.length; i++){
        box.addItem(portNames[i]);
    }
  }

  private boolean connectSerial(){
    if(dropDown.getSelectedItem() == null) return false;

    SerialPort serialPort = new SerialPort((String)dropDown.getSelectedItem());

    try{
      serialPort.openPort();
    } catch (SerialPortException ex){
      System.err.println(ex.getMessage());
      context.alert.displayMessage("Port not available");
      return false;
    }

    try{
      serialPort.setParams(    Serial.BAUD,
                           SerialPort.DATABITS_8,
                           SerialPort.STOPBITS_1,
                           SerialPort.PARITY_NONE);
      serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
      System.err.println("Flow Control mode: "+serialPort.getFlowControlMode());

      context.updatePort(serialPort);
      context.alert.displayMessage("Port opened");
      context.sender.sendWaypointList();

      refreshButton.setEnabled(false);
      dropDown.setEnabled(false);
    } catch(SerialPortException ex){
      System.err.println(ex.getMessage());
      context.alert.displayMessage(ex.getMessage());
      context.alert.displayMessage("Connection Failed");
      return false;
    }
    return true;
  }

  private boolean disconnectSerial(){
    try{
      context.closePort();
    } catch(SerialPortException ex){
      System.err.println(ex.getMessage());
      context.alert.displayMessage(ex.getMessage());
      return false;
    }

    dropDown.setEnabled(true);
    refreshButton.setEnabled(true);
    context.alert.displayMessage("Serial Port Closed");
    resetData();
    return true;
  }

  private JPanel makeDashPanel(){
    Color orange = new Color(255,155,30);
    sideGauge  = new RotatePanel(context.theme.roverSide,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    topGauge   = new RotatePanel(context.theme.roverTop,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    frontGauge = new RotatePanel(context.theme.roverFront,
                                 context.theme.gaugeBackground,
                                 context.theme.gaugeGlare);
    BackgroundPanel dataPanel = new BackgroundPanel(context.theme.gaugeSquare);
    GridBagConstraints c = new GridBagConstraints();
    JPanel dashPanel = new JPanel();

    dataPanel.setBorder(new EmptyBorder(dataBorderSize[0],
                                        dataBorderSize[1],
                                        dataBorderSize[2],
                                        dataBorderSize[3]) );
    dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
    latitude = new DataLabel("Lat:");
    latitude.setForeground(orange);
    latitude.setFont(context.theme.ocr);
    dataPanel.add(latitude);
    longitude = new DataLabel("Lng:");
    longitude.setForeground(orange);
    longitude.setFont(context.theme.ocr);
    dataPanel.add(longitude);
    heading = new DataLabel("Dir:");
    heading.setForeground(orange);
    heading.setFont(context.theme.ocr);
    dataPanel.add(heading);
    pitch = new DataLabel("Ptc:");
    pitch.setForeground(orange);
    pitch.setFont(context.theme.ocr);
    dataPanel.add(pitch);
    roll = new DataLabel("Rol:");
    roll.setForeground(orange);
    roll.setFont(context.theme.ocr);
    dataPanel.add(roll);
    speed = new DataLabel("MPH:");
    speed.setForeground(orange);
    speed.setFont(context.theme.ocr);
    dataPanel.add(speed);
    voltage = new DataLabel("Vcc:");
    voltage.setForeground(orange);
    voltage.setFont(context.theme.ocr);
    dataPanel.add(voltage);
    dataPanel.setOpaque(false);

    dashPanel.setLayout(new GridBagLayout());
    dashPanel.setOpaque(false);
    c.gridy = 1;
    dashPanel.add(dataPanel,c);
    c.gridy = 2;
    dashPanel.add(frontGauge,c);
    c.gridy = 3;
    dashPanel.add(topGauge,c);
    c.gridy = 4;
    dashPanel.add(sideGauge,c);

    return dashPanel;
    }

  private void resetData(){
    latitude.update(0);
    longitude.update(0);
    heading.update(0);
    topGauge.update(0);
    pitch.update(0);
    sideGauge.update(0);
    roll.update(0);
    frontGauge.update(0);
    speed.update(0);
    voltage.update(0);
  }

  public void updateDash(Serial.telemetry id){
    switch(id){
      case LATITUDE:
        latitude.update(context.telemetry[id]);
        mapPanel.updateRoverLatitude((double)context.telemetry[id]);
        break;
      case LONGITUDE:
        longitude.update(context.telemetry[id]);
        mapPanel.updateRoverLongitude((double)context.telemetry[id]);
        f.repaint();
        break;
      case HEADING:
        heading.update(context.telemetry[id]);
        topGauge.update(context.telemetry[id]+90);
        break;
      case PITCH:
        pitch.update(context.telemetry[id]-90);
        sideGauge.update(context.telemetry[id]-90);
        break;
      case ROLL:
        roll.update(context.telemetry[id]-90);
        frontGauge.update(context.telemetry[id]-90);
        break;
      case SPEED:
        speed.update(context.telemetry[id]);
        break;
      case VOLTAGE:
        voltage.update(context.telemetry[id]);
        break;
    }
  }

  public static void DisplayError(Exception e){
    final JFrame errorFrame = new JFrame("Data");
    errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    errorFrame.setLayout(new FlowLayout());
    errorFrame.setVisible(true);
    JPanel panel = new JPanel();
    JLabel text = new JLabel("Error: \n"+e.getMessage());
    panel.add(text);
    errorFrame.add(panel);
    errorFrame.pack();
  }

  public static void main(String[] args) {
    Dashboard se = new Dashboard();
    SwingUtilities.invokeLater(se);
  }
}
