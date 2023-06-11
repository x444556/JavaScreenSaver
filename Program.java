package net.ddns.x444556;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Program extends JFrame implements Runnable, MouseInputListener, KeyListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private boolean running;
	private BufferedImage image;
	public int[] pixels;
	public int windowX = 1000, windowY = 1000;
	public int imageX = 1000, imageY = 1000;
	private long totalFramesRendered = 0;
	private long lastRenderNanos = 0;
	private double avgDeltaTimeTotal=0, avgDeltaTimeDivisor=0;
	public double targetFPS = 50;
	public boolean doSimulate = true;
	public boolean isSimulating = false;
	public int particleCount = 3000;
	public Particle[] Particles;
	public int pixelsPerUnit = 12;
	public Vector3 minSpeed = new Vector3(-100, -100, -1);
	public Vector3 maxSpeed = new Vector3( 100,  100,  1);
	public Vector3 borderStart = new Vector3(-(imageX/pixelsPerUnit/2)-0.05, -(imageY/pixelsPerUnit/2)-0.05,   1);
	public Vector3 borderEnd   = new Vector3( (imageX/pixelsPerUnit/2)-0.05,  (imageY/pixelsPerUnit/2)-0.05,  10);
	
	public Program() {
		thread = new Thread(this);
		image = new BufferedImage(imageX, imageY, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
		Particles = new Particle[particleCount];
		for(int i=0; i<particleCount; i++) {
			Particles[i] = new Particle(borderStart, borderEnd, minSpeed, maxSpeed);
		}
		
		setSize(windowX, windowY);
		setResizable(false);
		setTitle("Particles");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true);
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.addComponentListener(this);
		lastRenderNanos = System.nanoTime();
	}
	private synchronized void start() {
		running = true;
		thread.start();
	}
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, windowX, windowY, null);
		bs.show();
		long deltaTimeNanos = System.nanoTime() - lastRenderNanos;
		lastRenderNanos = System.nanoTime();
		avgDeltaTimeTotal += deltaTimeNanos;
		avgDeltaTimeDivisor++;
		if(totalFramesRendered % 25 == 0) {
			setTitle((!running ? "[STOPPED] " : (!doSimulate ?"[PAUSED] " : "")) + "   Render: " + 
					(Math.round((1 / (deltaTimeNanos / 1000000000.0)) * 10)/10.0) + " FPS   avg: " + 
					(Math.round((1 / ((avgDeltaTimeTotal/avgDeltaTimeDivisor) / 1000000000.0)) * 10)/10.0) + " FPS   target: " + 
					targetFPS + " FPS");
		}
		totalFramesRendered++;
	}
	private void simulate() {
		isSimulating = true;
		
		for(int i=0; i<particleCount; i++) {
			Particles[i].update(lastRenderNanos / 1000000000.0);
		}
		
		isSimulating = false;
	}
	private Color getHeatMapColor(double value)
	{
		double red, green, blue;
		double [][] color = { {0,0,0.2}, {0,0,1}, {0,0.6,1}};
		// A static array of n colors:  (black, blue, cyan) using {r,g,b} for each.
		  
		int idx1;        // |-- Our desired color will be between these two indexes in "color".
		int idx2;        // |
		double fractBetween = 0;  // Fraction between "idx1" and "idx2" where our value is.
		  
		value = value + value;        // Will multiply value by 2.
		idx1  = (int) Math.floor(value);                  // Our desired color will be after this index.
		idx2  = idx1+1;                        // ... and before this index (inclusive).
		fractBetween = value - idx1;    // Distance between the two indexes (0-1).
		
		red   = (color[idx2][0] - color[idx1][0])*fractBetween + color[idx1][0];
		green = (color[idx2][1] - color[idx1][1])*fractBetween + color[idx1][1];
		blue  = (color[idx2][2] - color[idx1][2])*fractBetween + color[idx1][2];
		
		return new Color((float)red, (float)green, (float)blue);
	}
	private void simToImg() {
		/*for(int y=0; y<imageY; y++) {
			for(int x=0; x<imageX; x++) {
				pixels[y * imageX + x] = 0;
			}
		}*/
		Arrays.fill(pixels, 0);
		
		for(int i=0; i<imageY/2; i++) {
			for(int j=i; j<imageX-i; j++) {
				pixels[i*imageX + j] = new Color(100, 100, 100).getRGB();
				pixels[(imageY/2-i + imageY/2-1)*imageX + j] = new Color(120, 120, 120).getRGB();
			}
		}
		for(int i=0; i<imageX/2; i++) {
			for(int j=i; j<imageY-i; j++) {
				pixels[(imageY/2-j + imageY/2-1)*imageX + i] = new Color(110, 110, 110).getRGB();
				pixels[(imageY/2-j + imageY/2-1)*imageX + imageX-i-1] = new Color(110, 110, 110).getRGB();
			}
		}
		
		int backPixY = (int) ((borderEnd.Y - borderStart.Y) / borderEnd.Z * pixelsPerUnit); 
		int backPixX = (int) ((borderEnd.X - borderStart.X) / borderEnd.Z * pixelsPerUnit); 
		for(int y=-backPixY/2; y<backPixY/2; y++) {
			for(int x=-backPixX/2; x<backPixX/2; x++) {
				pixels[imageX / 2 + x + (imageY / 2 + y) * imageX] = new Color(90, 90, 90).getRGB();
			}
		}
		
		// double sqrt3 = Math.sqrt(3); // used instead of maxSpeed
		double maxSpeed = this.maxSpeed.MaxSpeed();
		for(int i=0; i<particleCount; i++) {
			Particle p = Particles[i];
			if(p.Position.Z >= 1) {
				int screenX = (int)(p.Position.X / p.Position.Z * pixelsPerUnit + imageX / 2);
				int screenY = (int)(p.Position.Y / p.Position.Z * pixelsPerUnit + imageY / 2);
				
				// System.out.println("[Part." + i + "] Pos.XYZ: " + p.Position.X + "  " + p.Position.Y + "  " + p.Position.Z);
				// System.out.println("          Speed.XYZ: " + p.Speed.X + "  " + p.Speed.Y + "  " + p.Speed.Z);
				// System.out.println("          screen.XY: " + screenX + "  " + screenY);
				
				int pc = getHeatMapColor(p.getSpeed()/maxSpeed).getRGB();
				int pix = (int) (pixelsPerUnit * ((borderEnd.Z - p.Position.Z) / borderEnd.Z));
				if(pix < 2) pix = 2;
				for(int py = -pix / 2; py < pix / 2 && screenY + py*pixelsPerUnit < imageY && screenY + py >= 0; py++) {
					for(int px = -pix / 2; px < pix / 2 && screenX + px*pixelsPerUnit < imageX && screenX + px >= 0; px++) {
						pixels[(screenY + py) * imageX + screenX + px] = pc;
								//new Color(0.0f, 0.0f, (float)((borderEnd.Z - p.Position.Z) / borderEnd.Z)).getRGB();
					}
				}
				/*Vector3 shadowPos = new Vector3(p.Position.X / p.Position.Z * borderEnd.Z, 
						p.Position.Y / p.Position.Z * borderEnd.Z, borderEnd.Z);
				int spix = (int) (pixelsPerUnit * ((borderEnd.Z - shadowPos.Z) / borderEnd.Z));
				if(spix < 2) spix = 2;
				for(int py = -spix / 2; py < spix / 2; py++) {
					for(int px = -spix / 2; px < spix / 2; px++) {
						int sp = (int) ((shadowPos.Y / shadowPos.Z * pixelsPerUnit + imageY / 2 + py) * imageX + 
								(shadowPos.X / shadowPos.Z * pixelsPerUnit) + imageX / 2 + px);
						Color sbc = new Color(pixels[sp]);
						pixels[sp] = new Color(sbc.getRed() / 2, sbc.getGreen() / 2, sbc.getBlue() / 2).getRGB();
								//new Color(0.0f, 0.0f, (float)((borderEnd.Z - p.Position.Z) / borderEnd.Z)).getRGB();
					}
				}*/
				
				// float c = 1 - (float)(p.Position.Z / borderEnd.Z);
				//pixels[screenY * imageX + screenX] = Color.WHITE.getRGB();//new Color(c, c, c).getRGB();
			}
		}
	}
	private void sort() {
		for(int i=0; i<Particles.length; i++) {
			for(int j=i + 1; j<Particles.length; j++) {
				if(Particles[i].Position.Z < Particles[j].Position.Z) {
					Particle p = Particles[i];
					Particles[i] = Particles[j];
					Particles[j] = p;
				}
			}
		}
	}
	public void run() {
		requestFocus();
		while(running) {
			long now = System.nanoTime();
			if(doSimulate) {
				
			}
			long beginSimulate = System.nanoTime();
			simulate();
			long endSimulate = System.nanoTime();
			long beginSort = System.nanoTime();
			sort();
			long endSort = System.nanoTime();
			long beginSimToImg = System.nanoTime();
			simToImg();
			long endSimToImg = System.nanoTime();
			
			long beginRender = System.nanoTime();
			render();
			long endRender = System.nanoTime();
			
			/*System.out.println("[simulate] " + (endSimulate - beginSimulate) + " ns");
			System.out.println("[sort] " + (endSort - beginSort) + " ns");
			System.out.println("[simToImg] " + (endSimToImg - beginSimToImg) + " ns");
			System.out.println("[render] " + (endRender - beginRender) + " ns");
			System.out.println();*/
			
			long t = System.nanoTime();
			long timeout = (long) (((1000000000.0 / targetFPS) - (t-now)) / 1000000);
			if(timeout > 0) {
				try {
					Thread.sleep(timeout);
				} catch (Exception e) {
					System.out.println("ns="+(1000000000.0 / targetFPS)+"  t="+t+"  now="+now+"  t-now="+(t-now)+
							"  *="+((int)((1000000000.0 / targetFPS) - (t-now)) / 1000000));
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static void main(String [] args) {
		Program sim = new Program();
		sim.start();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mousePressed(MouseEvent e) {
		int x = (int)(e.getX() * ((float)imageX / (float)windowX));
		int y = (int)(e.getY() * ((float)imageY / (float)windowY));
		if(SwingUtilities.isLeftMouseButton(e)) {
			
		}
		else if(SwingUtilities.isRightMouseButton(e)) {
			
		}
		else if(SwingUtilities.isMiddleMouseButton(e)) {
			
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent key) {
		if((key.getKeyCode() == KeyEvent.VK_W)) targetFPS++;
		if((key.getKeyCode() == KeyEvent.VK_S)) targetFPS--;
		
		if(key.getKeyCode() == KeyEvent.VK_ENTER) doSimulate = !doSimulate;
		
		if(targetFPS < 1) targetFPS = 1;
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentResized(ComponentEvent e) {
		// windowX = getSize().width;
		// windowY = getSize().height;
	}
	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
}
