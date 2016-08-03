import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class Facteur {
	
	 private Trieur monTrieur;


	private Socket socketAutoShelves;
	private DataInputStream is;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
	
	private static final int BUFFER_SIZE = 1100;
	
	int current = 0;
	byte [] myvideobytearray  = new byte [50000000];
	private Thread videoThread;
	
	private volatile boolean videoReception; 
		
		
		
		/**
		 * @brief setter
		 *
		 * @details Cree un nouveau facteurGestionnaireUtilisateur
		 *
		 * @return none
		 */
		private Facteur()
		{
		    monTrieur = new Trieur();
		    videoReception = false;
		    
		    try {
				fos = new FileOutputStream("/home/raelsan/source-downloaded.mp4");
				bos = new BufferedOutputStream(fos);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/**
		 * @class FacteurHolder
		 *
		 * @brief setter
		 *
		 * @details Cree une instance unique de Facteur
		 *
		 * @return none
		 */
		private static class FacteurHolder
		{
		    private final static Facteur instance = new Facteur();
		}
		
		/**
		 * @brief
		 *
		 * @details
		 *
		 * @return instance de Facteur
		 */
		public static Facteur getInstance()
		{
		    return FacteurHolder.instance;
		}
		
		public void setVideoReception(boolean value){
			videoReception = value;
		}
		
		
		/**
		 * @brief methode recevoirMessage
		 *
		 * @details Lance le thread de reception
		 *
		 * @param
		 *
		 * @return none
		 */
		public void recevoirMessage(){
		
		    Thread t = new Thread(new ThreadReception());
		
		    t.start();
		
		}

		
		
		/**
		 * @class ThreadReception
		 *
		 * @brief Classe implémentant le ThreadReception
		 *
		 * @details Contient le thread charge de recevoir les messages
		 * envoyes par le serveur
		 *
		 * @return String envoyee par le serveur
		 *
		 */
		private class ThreadReception implements Runnable {
		
		
		    /**
		     * @brief setter
		     *
		     * @details Cree un nouveau ThreadReception
		     *
		     * @return none
		     */
		    public ThreadReception(){
		    }
		
		    /**
		     * @brief fonction du thread
		     *
		     * @details recoit le message envoye par le serveur
		     *
		     * @return String envoye par le serveur
		     */
		    public void run(){
		
		        
		
		        while(!videoReception)
		        {
		        	
		        	byte [] myByteArray  = new byte [BUFFER_SIZE];
		        	
		            try
		            {
		            	
		                int bytes = is.read(myByteArray, 0, 1024);
		                
		                if(bytes != -1){
		                	monTrieur.trierReception(myByteArray);
		                }else{
		                	retablirConnexion();
		                }
		                
		            }
		            catch (IOException e)
		            {
		                e.printStackTrace();
		                retablirConnexion();
		            }
		        }
		
		    }
		
		}
		
		/**
		 * @brief initialisateur de communication
		 *
		 * @details déclenche le threadCommunication
		 *
		 *
		 * @return resultat de l'initialisation (succes ou pas)
		 */
		public void etablirConnexion(){
		
			
			ServerSocket socket_ecoute;
			try {
				System.out.println("création de la socket serveur");
				socket_ecoute = new ServerSocket (1248);
				socketAutoShelves = socket_ecoute.accept();
				System.out.println("client connecte");
				is = new DataInputStream(socketAutoShelves.getInputStream());
				recevoirMessage();
				socket_ecoute.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		public void retablirConnexion(){
		
			try {
				is.close();
				socketAutoShelves.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			etablirConnexion();
			
		}
		
		
		public void startVideoReception(){
			videoThread = new Thread(new ThreadVideoReception());
			videoThread.start();
		}
		
		private class ThreadVideoReception implements Runnable {
			
			
		
		    /**
		     * @brief fonction du thread
		     *
		     * @details recoit le message envoye par le serveur
		     *
		     * @return String envoye par le serveur
		     */
		    public void run(){
		
		        int bytesRead = 0;
		        

		    	
				try {
					bytesRead = is.read(myvideobytearray,0,myvideobytearray.length);
					current = bytesRead;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            

	            do {
	               try {
	            	Timer timer = new Timer();
			    	TimerReached task = new TimerReached();
					timer.schedule(task, 2000);
					
					bytesRead = is.read(myvideobytearray, current, (myvideobytearray.length-current));
					if(bytesRead >= 0){
						current += bytesRead;
						timer.cancel();
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	               
	            } while(videoReception);

	            
	            
		        
		
		    }
		
		}
		
		private class TimerReached extends TimerTask{

			@Override
			public void run() {
				try {
					bos.write(myvideobytearray, 0 , current);
					bos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
				videoThread.interrupt();
	            System.out.println("video recue");
	            setVideoReception(false);
	            recevoirMessage();
				
			}
			
		}
	


}
