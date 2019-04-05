package gnome.drive;

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class GnomeDrive{
    public static void main(String [] args)
    {
        JFileChooser fileChooser = new JFileChooser();

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION)
        {
            String filename = fileChooser.getSelectedFile().getPath();
            JOptionPane.showMessageDialog(null, "You selected " + filename);
        }
        else if (result == JFileChooser.CANCEL_OPTION)
        {   
//            JOptionPane.showMessageDialog(null, "You selected nothing.");
        }
        else if (result == JFileChooser.ERROR_OPTION)
        {
            JOptionPane.showMessageDialog(null, "An error occurred.");  
        }                             
    }
}