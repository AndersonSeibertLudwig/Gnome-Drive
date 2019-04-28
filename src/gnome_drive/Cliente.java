package gnome_drive;

import java.io.*;
import java.net.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Cliente {

    public static void main(String[] argv) throws Exception {
        // Cria a conexao com o servidor e as streams de input e output
        Socket socketCliente = new Socket("127.0.0.1", 6789);
        DataOutputStream paraServidor = new DataOutputStream(socketCliente.getOutputStream());
        DataInputStream doServidor = new DataInputStream(socketCliente.getInputStream());
        int opcao = -1;
        while (opcao != 2) {
            opcao = menu();
            switch (opcao) {
                case 0:
                    paraServidor.writeInt(1);
                    enviaArquivo(paraServidor, selectFile());
                    break;
                case 1:
                    paraServidor.writeInt(2);
                    String resposta = doServidor.readUTF();
                    if (resposta.equals("null")) {
                        System.out.println("Nao existem arquivos no servidor para serem transferidos!");
                    } else {
                        String[] respostas = resposta.split(";");
                        int opcao2 = JOptionPane.showOptionDialog(null, "Selecione um arquivo",
                                "Gnome Drive",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, respostas, respostas[0]);
                        paraServidor.writeInt(opcao2 + 1);
                        try {
                            recebeArquivo(doServidor);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
        // Fecha o socket e as streams
        paraServidor.close();
        doServidor.close();
        socketCliente.close();
    }

    private static void enviaArquivo(DataOutputStream paraServidor, File file) throws IOException {
        InputStream in = new FileInputStream(file);
        paraServidor.writeUTF(file.getName());
        paraServidor.writeLong(file.length());
        byte[] bytes = new byte[16 * 1024];
        int count;
        System.out.println("Enviando arquivo para o servidor...");
        while ((count = in.read(bytes)) > 0) {
            System.out.println("Enviando: " + count);
            paraServidor.write(bytes, 0, count);
        }
        in.close();
    }

    private static void recebeArquivo(DataInputStream doServidor) throws Exception {
        File arquivo = selectFile();
        OutputStream out = new FileOutputStream(arquivo);
        long quantidadeDeBytesRestantes = doServidor.readLong();

        byte[] bytes = new byte[16 * 1024];
        int count;
        while ((count = doServidor.read(bytes)) > 0) {
            System.out.println(count);
            out.write(bytes, 0, count);
            quantidadeDeBytesRestantes -= count;
            if (quantidadeDeBytesRestantes <= 0) {
                System.out.println("Arquivo recebido do servidor!");
                break;
            }
        }
        out.close();
    }

    private static File selectFile() throws Exception {
        JFileChooser fileChooser = new JFileChooser();

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            System.out.println(filename);
            return new File(filename);
        } else if (result == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(null, "Nenhum arquivo foi selecionado!");
            throw new Exception("Nenhum arquivo foi selecionado!");
        } else if (result == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(null, "Erro ao selecionar o arquivo!");
            throw new Exception("JFileChooser: Erro ao selecionar arquivo");
        }
        throw new Exception("Erro ao selecionar arquivo");
    }

    private static int menu() {
        String[] opcoes = {"Enviar Arquivo para o servidor", "Baixar arquivo do servidor", "Sair"};
        return JOptionPane.showOptionDialog(null, "Selecione uma opção",
                "Gnome Drive",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);
    }
}
