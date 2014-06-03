package com.ui;

import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.FontMetrics;

public class DataLabel extends JLabel{
  String label;
  String suffix;
  double data;
  public DataLabel(String prefix, double dat, String units){
    label = prefix;
    data = dat;
    suffix = units;
    setText(label + data  + suffix);
  }

  public DataLabel(String prefix, String units){
    label = prefix;
    data = 0.;
    suffix = units;
    setText(label + data + suffix);
  }

  public DataLabel(String prefix){
    label = prefix;
    data = 0.;
    suffix = new String();
    setText(label + data + suffix);
  }

  public void update(double dat){
    data = dat;
    setText(label + data + suffix);
  }

  public void setLabel(String prefix){
    label = prefix;
    setText(label + data + suffix);
  }

  public void setUnits(String units){
    suffix = units;
    setText(label + data + suffix);
  }

  public double getData(){
    return data;
  }
}
