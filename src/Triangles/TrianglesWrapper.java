package Triangles;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class TrianglesWrapper extends JFrame {

	public final int FRAMESIZE = 600;
	public final int BTNSPACE = 63;
	public final int HRZSPACE = 8;
	public final int NUMCOLORS = 2;
	
	public TrianglesWrapper() {
        setSize(3*FRAMESIZE/2+HRZSPACE, FRAMESIZE+BTNSPACE);
        add(new Triangles(FRAMESIZE, FRAMESIZE, NUMCOLORS));
//        add(new Triangles(FRAMESIZE, FRAMESIZE, NUMCOLORS));
        setResizable(false);
        setTitle("Sociology");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                TrianglesWrapper go = new TrianglesWrapper();
                go.setVisible(true);
            }
        });
	}
}
