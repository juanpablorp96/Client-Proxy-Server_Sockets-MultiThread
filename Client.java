import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    public static void main(String[] args) throws Exception {
//obtiene la direccion de la maquina cliente
        InetAddress inetAddress = InetAddress.getLocalHost();
        String addr = inetAddress.getHostAddress();

//primera comunicacion client-proxy-server-proxy-client
//con este se trae el menu
//si no es local, cambiar localhost por la ip de la maquina donde este el proxy
        Socket sck = new Socket("192.168.0.1", 7777);
        PrintStream ps = new PrintStream(sck.getOutputStream());
//mensaje + "-" que es para tokenizar la direccion que es addr y el "1" para que el proxy sepa que le esta hablando el cliente
        ps.println("Init" + "0" + "-" + addr + "1");
        sck.close();
//client escucha con este socket
        ServerSocket ss = new ServerSocket(9999);
        while (true) {

            Socket sock = null;

            try {

                sock = ss.accept();
//crea un hilo por cada vez que haya comunicacion entre socckets para atender el request
                Thread t = new ClientHandler(sock);
                t.start();
            } catch (Exception e) {
                sock.close();
                e.printStackTrace();
            }

        }

    }
}
class GlobalsClient {

    public static int ID;
    public static String password;
    public static String hash;
    public static int init = 0;


}
class ClientHandler extends Thread {

    final Socket sock;

    // Constructor 
    public ClientHandler(Socket sock) {
        this.sock = sock;

    }
private static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
    String hex = Integer.toHexString(0xff & hash[i]);
    if(hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
    return hexString.toString();
}
    @Override
    public void run() {

        try {
            String flag = "1";
            InetAddress inetAddress = InetAddress.getLocalHost();
            String addr = inetAddress.getHostAddress();

            //imprime el mensaje que recibe de respuesta
            InputStreamReader ir = new InputStreamReader(sock.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            String message = "";
            message = br.readLine();
            char count = message.charAt(message.length() - 1);
            String countS = String.valueOf(count);
            int c = Integer.parseInt(countS);
            c = c + 1;
            String c_final = Integer.toString(c);
            String new_message = message.substring(0, message.length() - 1);
            if(GlobalsClient.init != 0 && GlobalsClient.init != -1){
            System.out.println(new_message + "  Vote 1, 2 o 3 (ALTO, MEDIO, BAJO)");
            }
            
            if(GlobalsClient.init == 0){
            String[] values = new_message.split(";");
            GlobalsClient.ID = Integer.parseInt(values[0]);
            System.out.println("ID asignado: "+ values[0] + values[1]);
            GlobalsClient.init = -1;
            }
            
            
            //entrada por teclado
            Scanner scn = new Scanner(System.in);
            String tosend = scn.nextLine();
            //comunicacion con el proxy
            Socket sck = new Socket("192.168.0.1", 7777);
            PrintStream ps = new PrintStream(sck.getOutputStream());
            
            if(GlobalsClient.init == -1){
            GlobalsClient.password = tosend;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(tosend.getBytes(StandardCharsets.UTF_8));
            GlobalsClient.hash = bytesToHex(encodedhash);        
            ps.println("Password-" + GlobalsClient.hash + "-" + Integer.toString(GlobalsClient.ID) + "-" + addr + flag);
            GlobalsClient.init = 1;

            }
            else{
            ps.println(tosend + c_final + "-" + GlobalsClient.hash + "-" + Integer.toString(GlobalsClient.ID) + "-" + addr + flag);
            }

            sock.close();

        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}