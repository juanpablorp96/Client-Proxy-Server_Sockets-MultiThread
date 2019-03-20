import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proxy {

    public static void main(String[] args) throws Exception {
//socket donde escucha
        ServerSocket ss = new ServerSocket(7777);
        System.out.println("Proxy Inicia en "+ss.getInetAddress()+" "+ss.getLocalPort());
        while (true) {

            Socket sock = null;

            try {

                sock = ss.accept();
                System.out.println("Se conectó: " + sock);
                Thread t = new ProxyHandler(sock);
                t.start();
            } catch (Exception e) {
                sock.close();
                e.printStackTrace();
            }

        }

    }
}

class ProxyHandler extends Thread {

    final Socket sock;

    // Constructor 
    public ProxyHandler(Socket sock) {
        this.sock = sock;

    }

    @Override
    public void run() {

        try {

            InputStreamReader ir = new InputStreamReader(sock.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            String message = "";
            message = br.readLine();
            //System.out.println(message);
            //el ultimo caracter del mensaje es la bandera, si es 1 es del cliente, si es 2 es del server, 3 de la entidad
            char flag = message.charAt(message.length() - 1);
            //se le quita el ultimo caracter para limpiar el mensaje
            String new_message = message.substring(0, message.length() - 1);

            if (flag == '1') {
                //si el mensaje viene del cliente, se le envia al server
                Socket sck = new Socket("localhost", 8888);
                PrintStream ps = new PrintStream(sck.getOutputStream());
                ps.println(new_message);
                sock.close();
            }
            if (flag == '2') {
                //si el mensaje viene del server, se le envia al cliente
                //en el mensaje viene el addr del cliente, se tokeniza el "-"
                String[] values = new_message.split("-");
                String addr = values[1];
                //se envia al cliente correspondiente
                Socket sck = new Socket(addr, 9999);
                PrintStream ps = new PrintStream(sck.getOutputStream());

                ps.println(values[0]);
                sock.close();
            }
            if (flag == '3') {
                //mensaje viene de entidad, se envia al server
                Socket sck = new Socket("localhost", 8888);
                PrintStream ps = new PrintStream(sck.getOutputStream());
                ps.println(new_message);
                sock.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}