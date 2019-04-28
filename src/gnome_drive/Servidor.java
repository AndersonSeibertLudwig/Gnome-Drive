package gnome_drive;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Servidor extends Thread {
    private Socket socketCliente;
    private static ArrayList<File> arquivos = new ArrayList<>();
    private static int numCliente = 0;

    private Servidor(Socket cliente) {
        this.socketCliente = cliente;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] argv) throws Exception {
        ServerSocket servidor = new ServerSocket(6789);
        System.out.println("Server na porta " + servidor.getLocalPort());
        while (true) {
            Socket cliente = servidor.accept();
            numCliente += 1;
            Thread thread = new Thread(new Servidor(cliente));
            thread.start();
            if (!thread.isAlive()) {
                servidor.close();
            }
        }

    }

    @Override
    public void run() {
        System.out.println(
                "Cliente " + Servidor.numCliente + " Conectado: " + socketCliente.getInetAddress().getHostAddress());
        try {
            // Criando streams de input e output
            DataOutputStream paraCliente = new DataOutputStream(socketCliente.getOutputStream());
            DataInputStream doCliente = new DataInputStream(socketCliente.getInputStream());

            // Recebe a opcao escolhida pelo cliente
            int opcao = 1;
            while (opcao > 0 && opcao < 3) {
                opcao = doCliente.readInt();
                switch (opcao) {
                    case 1:
                        recebeArquivo(doCliente);
                        break;
                    case 2:
                        if (arquivos.isEmpty()) {
                            paraCliente.writeUTF("null");
                        } else {
                            StringBuilder nomeArquivos = new StringBuilder();
                            for (File arquivo : arquivos) {
                                nomeArquivos.append(arquivo.getName()).append(";");
                            }
                            paraCliente.writeUTF(nomeArquivos.toString());
                            int opcao2 = doCliente.readInt();
                            System.out.println(opcao2);
                            if (opcao2 > 0 && opcao2 <= arquivos.size()) {
                                enviaArquivo(paraCliente, opcao2 - 1);
                            }
                        }
                        break;
                }
            }
            System.out.println("Encerrando conexao  com o cliente!");
            doCliente.close();
            paraCliente.close();
            socketCliente.close();
            Servidor.numCliente -= 1;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void enviaArquivo(DataOutputStream paraCliente, int numArquivo) throws IOException {
        File file = arquivos.get(numArquivo);
        InputStream in = new FileInputStream(file);
        paraCliente.writeLong(file.length());
        byte[] bytes = new byte[16 * 1024];
        int count;
        System.out.println("Enviando arquivo para o cliente...");
        while ((count = in.read(bytes)) > 0) {
            System.out.println("Enviando: " + count);
            paraCliente.write(bytes, 0, count);
        }
        in.close();
        System.out.println("Enviado!");
    }

    private void recebeArquivo(DataInputStream input) throws IOException {
        InputStream doCliente = socketCliente.getInputStream();
        String fileName = input.readUTF();
        long quantidadeDeBytesRestantes = input.readLong();
        File file = new File(fileName);
        arquivos.add(file);
        OutputStream out = new FileOutputStream(file);

        byte[] bytes = new byte[16 * 1024];
        int count;
        while ((count = doCliente.read(bytes)) > 0) {
            out.write(bytes, 0, count);
            quantidadeDeBytesRestantes -= count;
            if (quantidadeDeBytesRestantes <= 0) {
                System.out.println("Arquivo recebido do cliente!");
                break;
            }
        }
        out.close();
    }

}