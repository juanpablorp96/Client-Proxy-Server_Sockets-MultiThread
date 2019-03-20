import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {
        //socket por el que escucha
        ServerSocket ss = new ServerSocket(8888);
        System.out.println("Servidor Inicia en "+ss.getInetAddress()+" "+ss.getLocalPort());

        while (true) {
            Socket sock = null;

            try {
                // socket object to receive incoming client requests 
                sock = ss.accept();
                System.out.println("Se conectó Proxy : " + sock);
                Thread t = new ServerHandler(sock);
                t.start();
            } catch (Exception e) {
                sock.close();
                e.printStackTrace();
            }
        }
    }
}

class GlobalsServer {

    public static List<String> consultas = new ArrayList<String>();
    public static int Votos[][] = new int [100][3];
    public static int ID = 1;
    public static String passwords[] = new String[10];

}

class ServerHandler extends Thread {

    final Socket sock;

    // Constructor 
    public ServerHandler(Socket sock) {
        this.sock = sock;

    }

    @Override
    public void run() {
        String option;
        while (true) {

            try {

                InputStreamReader ir = new InputStreamReader(sock.getInputStream());
                BufferedReader br = new BufferedReader(ir);
                option = br.readLine();
                Socket sck = new Socket("localhost", 7777);
                PrintStream ps = new PrintStream(sck.getOutputStream());
                
                //se obtiene el mensaje y la direccion tokenizando
                String[] values = option.split("-");
                String received = values[0];
                String addr = "";
                char index = ' ';
                String indexS = "";
                int ind = 0;
                int id_user = 0;
                String psw = "";
                String new_received ="";
                String flag = "2";
                String hash = "";
          

                    if(values.length == 4 && !values[0].equals("Password")){
                        addr = values[3];
                        id_user = Integer.parseInt(values[2]);
                        hash = values[1];
                        index = received.charAt(received.length() - 1);
                        indexS = String.valueOf(index);
                        ind = Integer.parseInt(indexS);
                        new_received = received.substring(0, received.length() - 1);
                    }
                    if(values.length == 2 && values[0].equals("Consolidar")){
                        addr = values[1];
                    }
                    if(values.length == 2 && !values[0].equals("Consolidar")){
                        addr = values[1];
                        index = received.charAt(received.length() - 1);
                        indexS = String.valueOf(index);
                        ind = Integer.parseInt(indexS);
                        new_received = received.substring(0, received.length() - 1);
                    }
                    if(values.length == 3){
                        addr = values[2];                  
                    }

                    // receive the answer from client
                    if (received.equals("Consultas") ) {

                        //Guardar consultas que llegan
                        String consultasNuevas[] = values[1].split(";");
                        for(String cNueva:consultasNuevas){
                            //Agregar cada consulta a la coleccion de consultas
                            GlobalsServer.consultas.add(cNueva);
                            }
                        ps.println("Consultas enviadas exitosamente . . .    Escriba 'Consolidar' para ver resultados " + "-" + addr + flag);
                        sock.close();
                        break;
                    } 
                    if (received.equals("Consolidar")) {
                        String send = "Consolidados:|";
                        for(int i = 0; i < GlobalsServer.consultas.size(); i++){
                            send += GlobalsServer.consultas.get(i) + " >>> ALTO: " + Integer.toString(GlobalsServer.Votos[i][0]) + " MEDIO: " + Integer.toString(GlobalsServer.Votos[i][1]) + " BAJO: " + Integer.toString(GlobalsServer.Votos[i][2])+ "|";
                        }
                        send += "-" + addr + flag;
                        ps.println(send);
                        sock.close();
                        break;
                    }
                    if (new_received.equals("Exit")) {
                        sock.close();
                        System.out.println("Connection closed");
                        break;
                    }
                    if (new_received.equals("Init")) {
                        ps.println(GlobalsServer.ID + ";" + " Ingrese una contraseña" + "0" + "-" + addr + flag);
                        GlobalsServer.ID += 1;
                        sock.close();
                        break;
                    }
                    if (received.equals("Password")) {
                    psw = values[1];
                    id_user = Integer.parseInt(values[2]);
                    addr = values[3];
                    System.out.println("hash > " + psw);

                        GlobalsServer.passwords[id_user] = psw;
                        ps.println("Usuario registrado exitosamente . . .  Nueva consulta > " + GlobalsServer.consultas.get(0) + "0" + "-" + addr + flag);
                        sock.close();
                        break;
                    }
                    if(GlobalsServer.passwords[id_user].equals(hash)){
                        System.out.println("Hash valido");

                    if (new_received.equals("1")) {

                        GlobalsServer.Votos[ind - 1][0] += 1;
                        ps.println("Voto registrado exitosamente . . .  Nueva consulta > " + GlobalsServer.consultas.get(ind) + indexS + "-" + addr + flag);
                        sock.close();
                        break;
                    }
                    if (new_received.equals("2")) {
                        System.out.println(ind);
                        System.out.println(indexS);


                        GlobalsServer.Votos[ind - 1][1] += 1;
                        ps.println("Voto registrado exitosamente . . .  Nueva consulta > " + GlobalsServer.consultas.get(ind) + indexS + "-" + addr + flag);
                        sock.close();
                        break;
                    }
                    if (new_received.equals("3")) {
                        GlobalsServer.Votos[ind - 1][2] += 1;
                        ps.println("Voto registrado exitosamente . . .  Nueva consulta > " + GlobalsServer.consultas.get(ind) + indexS + "-" + addr + flag);
                        sock.close();
                        break;
                    }
                    }
                  
               
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}