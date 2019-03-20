import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;



//-------------------------------------------------------------------------------------------

public class Entity {

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
        String flag = "3";

        String[] consultas = leerArchivo(args[0]+".txt");
        String tosend = "";
        for (String c : consultas) {
            tosend +=  c + ";";
        }
        ps.println("Consultas-" + tosend + "-" + addr + flag);
        sck.close();
//Entidad escucha con este socket
        ServerSocket ss = new ServerSocket(9999);
        while (true) {

            Socket sock = null;

            try {

                sock = ss.accept();
//crea un hilo por cada vez que haya comunicacion entre socckets para atender el request
                Thread t = new EntityHandler(sock,args[0]);
                t.start();
            } catch (Exception e) {
                sock.close();
                e.printStackTrace();
            }

        }

    }
        private static String[] leerArchivo(String nombreArchivo) {
        Path ruta = Paths.get(nombreArchivo);
        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            String[] lines = lineas.toArray(new String[lineas.size()]);
            return lines;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

class EntityHandler extends Thread {

    final Socket sock;
    private String entidad;

    // Constructor 
    public EntityHandler(Socket sock, String entidad) {
        this.sock = sock;
        this.entidad = entidad;

    }

    @Override
    public void run() {

        try {
            String flag = "3";
            InetAddress inetAddress = InetAddress.getLocalHost();
            String addr = inetAddress.getHostAddress();

            //imprime el mensaje que recibe de respuesta
            InputStreamReader ir = new InputStreamReader(sock.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            String message = "";
            message = br.readLine();
            if(message.startsWith("Consultas")){
               System.out.println(message);
            }else if(message.startsWith("Consolidados")){
                StringTokenizer tokens = new StringTokenizer(message,"|");
                String token;
                while(tokens.hasMoreTokens()){
                    token = tokens.nextToken();
                    if(token.startsWith(this.entidad))
                       System.out.println(token);
                }

            }
            
            //entrada por teclado
            Scanner scn = new Scanner(System.in);
            String tosend = scn.nextLine();
            //comunicacion con el proxy
            Socket sck = new Socket("192.168.0.1", 7777);
            PrintStream ps = new PrintStream(sck.getOutputStream());
            ps.println(tosend + "-" + addr + flag);
            sock.close();

        } catch (IOException ex) {
            Logger.getLogger(EntityHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}