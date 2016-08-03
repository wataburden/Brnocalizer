
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


public class Trieur {
	
	
	static public final String CONNECTION = "0";
    static public final String ACCELEROMETER = "ACCELEROMETERDATA";
    static public final String COMPASS = "COMPASSDATA";
    static public final String GYROSCOPE = "GYRODATA";
    static public final String GPS = "GPSDATA";
    static public final String VIDEO = "VIDEODATA";
    static public final String STOP = "STOP";
    static public final String ENDVIDEO = "ENDVIDEO";
    
    private PrintWriter accPw;
    private PrintWriter cmpPw;
    private PrintWriter gyrPw;
    private PrintWriter gpsPw;
    
    StringBuilder sb;
    
    private Date date;
    
    private ArrayList <byte[]> videoData;
    
    byte[] videoReceiver;
    
	
	public Trieur(){
	    
	    initializeFileWriters();
	    
	}
	
	
	public void trierReception(byte[] reception){
		
		String display1 = new String(reception, StandardCharsets.UTF_8);
		if(display1.contains(" ")){
			String[] analyse = display1.split(" ");
			String[] tab = analyse[1].split(",");
			sb = new StringBuilder();
			date = new Date();
			
			switch (analyse[0]){
				case CONNECTION :
					break;
					
				case ACCELEROMETER:
					sb.append(tab[0]);
					sb.append(',');
					sb.append(tab[1]);
					sb.append(',');
					sb.append(tab[2]);
					sb.append(',');
					sb.append(new Timestamp(date.getTime()));
					sb.append('\n');
					accPw.write(sb.toString());
					break;
					
					
				case COMPASS:
					sb.append(tab[0]);
					sb.append(',');
					sb.append(tab[1]);
					sb.append(',');
					sb.append(tab[2]);
					sb.append(',');
					sb.append(new Timestamp(date.getTime()));
					sb.append('\n');
					cmpPw.write(sb.toString());

					break;
					
				case GYROSCOPE :
					sb.append(tab[0]);
					sb.append(',');
					sb.append(tab[1]);
					sb.append(',');
					sb.append(tab[2]);
					sb.append(',');
					sb.append(new Timestamp(date.getTime()));
					sb.append('\n');
					gyrPw.write(sb.toString());

					break;
					
				case GPS :
					System.out.println("GPS");
					sb.append(tab[0]);
					sb.append(',');
					sb.append(tab[1]);
					sb.append(',');
					sb.append(tab[2]);
					sb.append(',');
					sb.append(new Timestamp(date.getTime()));
					sb.append('\n');
					gpsPw.write(sb.toString());

					break;
					
				case VIDEO :
					int position = Integer.parseInt(analyse[2]);
					byte[] data = analyse[1].getBytes();
					videoData.add(position, data);
					break;
					
				case ENDVIDEO :	
					Facteur.getInstance().setVideoReception(false);
					break;
					
				case STOP :
					accPw.close();
					gpsPw.close();
					cmpPw.close();
					gyrPw.close();
					Facteur.getInstance().setVideoReception(true);
					Facteur.getInstance().startVideoReception();
					break;
					
				default :
					break;

			}
		}
		
	}
	
	public void initializeFileWriters(){
		
		try {
			accPw = new PrintWriter(new File("/home/raelsan/accData.csv"));
			sb = new StringBuilder();
			
			sb.append("accX");
			sb.append(',');
			sb.append("accY");
			sb.append(',');
			sb.append("accZ");
			sb.append(',');
			sb.append("time");
			sb.append('\n');
			
			accPw.write(sb.toString());
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    try {
			cmpPw = new PrintWriter(new File("/home/raelsan/cmpData.csv"));
			cmpPw.append("cmpX");
			cmpPw.append(',');
			cmpPw.append("cmpY");
			cmpPw.append(',');
			cmpPw.append("cmpZ");
			cmpPw.append(',');
			cmpPw.append("time");
			cmpPw.append('\n');
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    try {
			gyrPw = new PrintWriter(new File("/home/raelsan/gyrData.csv"));
			gyrPw.append("gyrX");
			gyrPw.append(',');
			gyrPw.append("gyrY");
			gyrPw.append(',');
			gyrPw.append("gyrZ");
			gyrPw.append(',');
			gyrPw.append("time");
			gyrPw.append('\n');
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		try {
			gpsPw = new PrintWriter(new File("/home/raelsan/gpsData.csv"));
			sb = new StringBuilder();
			
			sb.append("latitude");
			sb.append(',');
			sb.append("longitude");
			sb.append(',');
			sb.append("altitude");
			sb.append(',');
			sb.append("time");
			sb.append('\n');
			
			gpsPw.write(sb.toString());
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    
	}

}