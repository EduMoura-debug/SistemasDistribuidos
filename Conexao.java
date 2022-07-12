import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
/*
import java.io.*;
import java.net.*;
import java.util.*;
*/
public class Conexao {
    
    private boolean conectado = true;
    public static final String PERMITIR_ACESSO = "PERMITIR";
    public static final String NEGAR_ACESSO = "NAO_PERMITIR";
    private static final int PORTA = 8000;
    private Socket sock;
    private ServerSocket listenSocket;

    public void conectar(Processo coordenador){
        System.out.println("Coordenador " + coordenador + " pronto para receber requisicoes.");
        new Thread( new Runnable() {
            @Override
            public void run() {
                try{
                    listenSocket = new ServerSocket(PORTA);
                    while(conectado){

                        sock = listenSocket.accept();

                        InputStreamReader s = new InputStreamReader(sock.getInputStream());
                        BufferedReader rec  = new BufferedReader(s);

                        String rBuf = rec.readLine();
                        System.out.println(rBuf);

                        DataOutputStream d = new DataOutputStream(sock.getOutputStream());
                        String sBuf = "Error!\n";
                        if(coordenador.isRecursoEmUso()){
                            sBuf = NEGAR_ACESSO + "\n";
                        
                        }else{
                            sBuf = PERMITIR_ACESSO + "\n";
                        }
                        d.write(sBuf.getBytes("UTF-8"));

                    }
                    System.out.println("Conexao encerrada.");
                } catch (IOException e) {
                    System.out.println("Conexao encerrada.");
                }
            }
        }).start();
    }

    public String realizarRequisicao(String mensagem){
        String rBuf = "ERRO!";
        try{
            Socket sock = new Socket("localhost", PORTA);

            DataOutputStream d = new DataOutputStream(sock.getOutputStream());
            d.write(mensagem.getBytes("UTF-8"));
            
            InputStreamReader s = new InputStreamReader(sock.getInputStream());
            BufferedReader rec = new BufferedReader(s);

            rBuf = rec.readLine();
            sock.close();
        } catch(Exception e) {
            System.out.println("A requisicao nao foi finalizada corretamente");
        }
        return rBuf;
    }

    public void encerrarConcexao(){
        conectado = false;
        try{
            sock.close();
        }catch (IOException | NullPointerException e) {
            System.out.println("Erro ao encerrar conexao: ");
            e.printStackTrace();
        }
        try{
            listenSocket.close();
        }catch (IOException | NullPointerException e) {
            System.out.println("Erro ao encerrar conexao: ");
            e.printStackTrace();
        }
    }


}
