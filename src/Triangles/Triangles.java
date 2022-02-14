// Derek Denoon
package Triangles;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("serial")
public class Triangles extends JPanel implements ActionListener {
	
	//2d array based variant
	private JButton startBtn;
	private JButton stopBtn;
	private JButton resetBtn;
	private JButton speedBtn;
	private JButton imageBtn;
	private JTextField sizeTxt;
	private JTextField sidesTxt;
	private JTextField alikeTxt;
	private JTextField genSkipTxt;
	private JTextField triangleSizeTxt;
	private JTextField neighborTxt;
	private JTextField colorsTxt; 		//if we want to deal with more than two colors
	private JLabel generations;			//states number of generations
	private JLabel picLabel;			//where the drawing happens
	private JLabel mouseLabel; 			//show cell coordinates of mouse
	private JLabel numMoveLabel;		//show number of agents that want to move
	private int vOffset;				//for mouse
	private int hOffset;				//for mouse
	private int vMax;					//vertical size of picture space 
	private int hMax;					//horizontal size of picture space
	private int genCount;				//same generation stuff from game of life
	private int genSkip;				//same generation stuff from game of life
	private int[] mouseCoords;			//where the mouse is
	private int size;					//how big each cell is in pixels
	private int[][] cells;				//the cells array
	private int[] speeds = new int[4];	//...speeds
	private int speedIndex;				//linked to which speed
	private Image pic;					//var to display the cells grid
	private Color[] colors;				//the different colors to use
	private int colorsToUse;			//for more than 2 colors
	private double density;				//percent of the board that will have an agent (roughly)
	private double alike; 				//percentage of neighbors who needs to be alike in color
	private int triangleSize;  				//how far away an agent can move from their current location
	private int neighborRadius;			//how big of a square around a location to use for neighbors
	private ArrayList<Integer> unhappy;	//a way to keep track of how many unhappy cells per gen
	private Timer timer;
	private boolean isRunning;

	// triangles specific
	private ArrayList<Point> points = new ArrayList<>();
	private int round = 0;
	private Point recentPoint = new Point(0,0);
	private ArrayList<Point> polygon = new ArrayList<>();
	private int sides;
	
    public Triangles(int xDim, int yDim, int numColors) {
        super(new GridBagLayout());                       				// set up graphics window
        setBackground(Color.LIGHT_GRAY);
		addMouseListener(new MAdapter());
		addMouseMotionListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
		//initialize the colors using rgb
//		colors = new Color[] {
//				new Color(0, 0, 0),
//				new Color(0, 0, 255),
//				new Color(255, 0, 0),
//				new Color(0, 255, 0),
//		};
		colors = new Color[] {
				new Color(0, 0, 0),
				new Color(0, 0, 255),
				new Color(255, 0, 0),
				new Color(0, 255, 0),
				new Color(255, 0, 255),
				new Color(255, 255, 0),
				new Color(0, 255, 255),
				new Color(255, 255, 255)
		};
		colorsToUse = numColors + 1;		//+1 for the empty cell color
		initBtns();
		initTxt();
		initLabels();
		pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
		picLabel = new JLabel(new ImageIcon(pic));
		addThingsToPanel();
		//a lot of initialization
		genCount = 0;
		genSkip = 1;
		vMax = yDim;
		hMax = xDim;
		sides = Integer.parseInt(sidesTxt.getText());
		alike = Double.parseDouble(alikeTxt.getText()) / 100;
		triangleSize = Integer.parseInt(triangleSizeTxt.getText());
		neighborRadius = Integer.parseInt(neighborTxt.getText());
		size = Integer.parseInt(sizeTxt.getText());
		unhappy = new ArrayList<Integer>();
		isRunning = false;
		for (int i = 0; i < 4; i++) {					//set the speed variation
			speeds[3-i] = 100 * i * i;
		}
		cells = new int[vMax / size][hMax / size];		//initialize the cells
		resetSim();										//initialize the simulation
		drawCells(pic.getGraphics());					//draw the initial set up
		timer = new Timer(speeds[speedIndex], this);	//initialize the timer
		timer.start();//start up the sim

    }

    //gridbags are so much fun
    public void addThingsToPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(1, 1, 0, 1);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 6;
		c.gridheight = 10;
		add(picLabel, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(0, 2, 0, 2);
		c.gridx = 0;
		c.gridy = 0;
		add(startBtn, c);
		c.gridx = 1;
		c.gridy = 0;
		add(stopBtn, c);
		c.gridx = 2;
		add(resetBtn, c);
		c.gridx = 3;
		add(speedBtn, c);
		c.insets = new Insets(0, 10, 0, 10);
		c.gridx = 4;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
		add(generations, c);
		c.gridx = 5;
		add(mouseLabel, c);
		c.gridx = 6;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(imageBtn, c);
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel("useless 1"), c);
		c.gridy = 3;
		add(new JLabel("Sides"), c);
		c.gridy = 4;
		add(new JLabel("useless 2"), c);
		c.gridy = 5;
		add(new JLabel("probably doesnt do anything"), c);
		c.gridy = 6;
		add(new JLabel("useless 3"), c);
		c.gridy = 7;
		add(new JLabel("Cell size (dont change)"), c);
		c.gridy = 8;
		add(new JLabel("useless 4"), c);
		c.gridy = 9;
		add(new JLabel("useless 5"), c);
		c.gridx = 7;
		c.gridy = 2;
		add(genSkipTxt, c);    	
		c.gridy = 3;
		add(sidesTxt, c);
		c.gridy = 4;
		add(alikeTxt, c);
		c.gridy = 5;
		add(triangleSizeTxt, c);
		c.gridy = 6;
		add(neighborTxt, c);
		c.gridy = 7;
		add(sizeTxt, c);
		c.gridy = 8;
		add(colorsTxt, c);
		c.gridy = 9;
		add(numMoveLabel, c);
    }
    
    public void initTxt() {
    	sidesTxt = new JTextField("3", 4); //the percentage of the cells grid to fill with living cells
    	sidesTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			sides = Integer.parseInt(sidesTxt.getText());
				resetSim();
				drawCells(pic.getGraphics());
    		}
    	});
    	alikeTxt = new JTextField("63", 4); //the percentage of cells of the same color (within its radius) a cell will be happy being near
    	alikeTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			alike = Double.parseDouble(alikeTxt.getText()) / 100;
    		}
    	});
    	genSkipTxt = new JTextField("1", 4); //how many generations to skip for display purposes
    	genSkipTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			genSkip = Integer.parseInt(genSkipTxt.getText());
    		}
    	});
    	triangleSizeTxt = new JTextField("6", 4); //how far a cell may relocate from its original location
    	triangleSizeTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			triangleSize = Integer.parseInt(triangleSizeTxt.getText());
    		}
    	});
    	neighborTxt = new JTextField("1", 4); //how far away from itself a cell looks to determine its like color percentage (in a square)
    	neighborTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			neighborRadius = Integer.parseInt(neighborTxt.getText());
    		}
    	});
    	sizeTxt = new JTextField("1", 4); //how large each cell in the grid is in pixels
    	sizeTxt.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			size = Integer.parseInt(sizeTxt.getText());
				resetSim();
				drawCells(pic.getGraphics());
    		}
    	});
    	colorsTxt = new JTextField(String.valueOf(colorsToUse - 1), 4); //how many different colors to use, + 1 for the empty color
   		colorsTxt.addActionListener(new ActionListener() {
   			public void actionPerformed(ActionEvent e) {
   				int temp = Integer.parseInt(colorsTxt.getText()) + 1;
   				if (temp > colors.length) {
   					numMoveLabel.setText("7 colors max");
   				} else {
   					colorsToUse = Integer.parseInt(colorsTxt.getText()) + 1;
   					numMoveLabel.setText("unknown");
   					resetSim();
   					drawCells(pic.getGraphics());
   				}
   			}
   		});
    }

    public void initLabels() {
    	generations = new JLabel("Generations: " + genCount);
    	mouseCoords = new int[2];
    	mouseLabel = new JLabel("Mouse off-grid");
    	numMoveLabel = new JLabel("unknown");
    }
    
    public void initBtns() { //similar stuff to game of life
		startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = true;
			}
		});
		
		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = false;
			}
		});
		
		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetSim();
				drawCells(pic.getGraphics());
			}
		});    	
		
		imageBtn = new JButton("Doesnt work");
		imageBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Calendar c = Calendar.getInstance();
					String fileName = ".\\d=" + density + " a=" + alike + " r=" + neighborRadius + " @" + c.get(Calendar.HOUR) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND)+ ".png";
					System.out.println(fileName);
					File outputFile = new File(fileName);
					outputFile.createNewFile();
					ImageIO.write((RenderedImage) pic, "png", outputFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		speedBtn = new JButton("Speed = Fast");
		speedIndex = 2;
		speedBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				speedIndex = (speedIndex + 1) % 4;
				timer.setDelay(speeds[speedIndex]);
				switch (speedIndex) {
				case 0 : {
					speedBtn.setText("Speed = Slow");
					break;
				}
				case 1 : {
					speedBtn.setText("Speed = Med");
					break;
				}
				case 2 : {
					speedBtn.setText("Speed = Fast");
					break;
				}
				case 3 : {
					speedBtn.setText("Speed = Whoa");
					break;
				}
				}
			}
		});
    }
    
    public Triangles() {
        super();
        setBackground(Color.WHITE);
		addMouseListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
	}
 
    public void paintComponent(Graphics g) { 	                 // draw graphics in the panel
        super.paintComponent(g);                              	 // call superclass' method to make panel display correctly
    }

    public void drawCells(Graphics g) {
    	for (int i = 0; i < cells.length; i++) {
    		for (int j = 0; j < cells[i].length; j++) {
		    	g.setColor(colors[cells[i][j]]);
				g.fillRect(i*size, j*size, size, size);
    		}
    	}
    }

    public void resetSim() {
    	if (sides > 7){
    		throw new IndexOutOfBoundsException("too many sides");
		}
		cells = new int[hMax / size][vMax / size];
		Point pivot = new Point(300,300);

		int increment = 360/sides;

		polygon.clear();

		for (int i = 0; i < sides; i++) {
			Point p = new Point( (int) (pivot.x + (270 * (Math.cos(Math.toRadians(increment*i))))), (int) (pivot.y + (270 * (Math.sin(Math.toRadians(increment*i))))));
			polygon.add(p);
		}

		for (int i = 0; i < polygon.size(); i++) {
			cells[polygon.get(i).x][polygon.get(i).y] = (i+1);
		}

		recentPoint.setLocation(polygon.get(0).x,polygon.get(0).y);

//		//left (0)
//    	cells[0][0] = 1;
//    	//right (1)
//    	cells[((hMax/size) / triangleSize) - 1][0] = 2;
//    	// middle (2)
//    	cells[(((hMax/size) / triangleSize) - 1)/2][(int) ((((hMax/size) / triangleSize) - 1)* (Math.sqrt(3.0)/2))] = 3;
    }
    
    public void updateCells() {
		int rand = ((int) (Math.random() * (sides)));
		Point p = new Point();
		if (round == 0){
			p = new Point(300,300);
			round++;
		}

		p.setLocation((recentPoint.x + polygon.get(rand).x)/2,(recentPoint.y + polygon.get(rand).y)/2);

		recentPoint.setLocation(p.getLocation());



		points.add(p);
		cells[p.x][p.y] = rand + 1;








//		int rand = ((int) (Math.random() * (3)));
//    	Point p = new Point();
//    	if (round == 0){
//    		p = new Point(0,0);
//    		round++;
//		}
//
//
//    	if (rand == 0){
//    		p.setLocation(((recentPoint.x)/2),(recentPoint.y)/2);
//		}else if (rand == 1){
//    		p.setLocation(((((hMax/size) / triangleSize) - 1) + recentPoint.x)/2,((recentPoint.y)/2));
//		}else if(rand == 2){
//    		p.setLocation((((((hMax/size) / triangleSize) - 1)/2) + recentPoint.x)/2,(((((hMax/size) / triangleSize) - 1)* (Math.sqrt(3.0)/2)) + recentPoint.y)/2);
//
//		}
//
//		recentPoint.setLocation(p.getLocation());
//
//
//
//    	points.add(p);
//    	cells[p.x][p.y] = rand + 1;


    }

    
    public void updateLabels() { //keep labels updated with the latest counts and other statistics!
    	generations.setText("Generations: " + genCount);
    	if ((mouseCoords[0] >= 0) && (mouseCoords[0] <= cells.length) && (mouseCoords[1] >= 0) && (mouseCoords[1] <= cells[0].length)) {
        	mouseLabel.setText("Mouse at (" + mouseCoords[0] + ", " + mouseCoords[1] + ")");    	    		
    	} else {
    		mouseLabel.setText("Mouse off-grid");
    	}
    	if (unhappy.size() > 0) {
    		numMoveLabel.setText("" + unhappy.get(unhappy.size()-1));
    	}
    }
    
    //fairly straightforward and similar to the actionPerformed methods in other projects
	@Override
	public void actionPerformed(ActionEvent e) {
		if (isRunning) {
			updateCells();			
	        drawCells(pic.getGraphics());
		}
		hOffset = picLabel.getLocationOnScreen().x - getLocationOnScreen().x;
		vOffset = picLabel.getLocationOnScreen().y - getLocationOnScreen().y;
		updateLabels();
		repaint();


	}
	
	//where the mouse handler goes
	//lots of old stuff from game of life, maybe you'll use them, maybe not
	private class MAdapter extends MouseAdapter {


		@Override
		public void mouseMoved(MouseEvent e) {
			Point p = new Point((e.getX() - hOffset) / size, (e.getY() - vOffset) / size);
//			System.out.println(hOffset + " " + e.getXOnScreen() + ", " + e.getYOnScreen() + " grid " + p.x*size + ", " + p.y*size);
			if ((e.getX() - hOffset) < 0) {
				mouseCoords[0] = -1; 
			} else {
				mouseCoords[0] = p.x;
			}
			if ((e.getY() - hOffset) < 0) {
				mouseCoords[1] = -1; 
			} else {
				mouseCoords[1] = p.y;
			}
		}
//		
//		@Override
//		public void mouseDragged(MouseEvent e) {
//			Point p = new Point((e.getX() - hOffset) / size, (e.getY() - vOffset) / size);
//			mouseCoords[0] = p.x;
//			mouseCoords[1] = p.y;			
//			try {
//				if (mouseDraw) {
//					cells[p.x][p.y] = 1; 
//				} else {
//					cells[p.x][p.y] = 0;
//				}
//				drawCells(pic.getGraphics());
//			} catch (ArrayIndexOutOfBoundsException e2) {
//			}
//		}

//		@Override
//		public void mouseReleased(MouseEvent e) {
//		}
	}
}

