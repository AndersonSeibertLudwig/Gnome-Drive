package gnome_drive;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Cliente {
	public final static int FILE_SIZE = Integer.MAX_VALUE;

	public static void main(String argv[]) throws Exception {
		// Cria a conexao com o servidor e as streams de input e output
		Socket socketCliente = new Socket("127.0.0.1", 6789);
		DataOutputStream paraServidor = new DataOutputStream(socketCliente.getOutputStream());
		DataInputStream doServidor = new DataInputStream(socketCliente.getInputStream());
		Scanner scan = new Scanner(System.in);
		int opcao = -1;
		while (opcao != 2) {			
			opcao = menu();
			switch (opcao) {
			case 0:
				paraServidor.writeUTF("" + 1);
				enviaArquivo(paraServidor, selectFile());
				break;
			case 1:
				paraServidor.writeUTF("" + 2);
				 String resposta = doServidor.readUTF();
				if (resposta.equals("null")) {
					System.out.println("Nao existem arquivos no servidor para serem transferidos!");
				} else {
                                        String respostas[] = resposta.split(";");
					int opcao2 = JOptionPane.showOptionDialog(null, "Selecione um arquivo",
                                    "Gnome Drive",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, respostas, respostas[0]);
					paraServidor.writeUTF("" + opcao2+1);
					recebeArquivo(socketCliente);
				}
				break;
			}

		}
		// ENVIA MENSAGEM PARA ENCERRAR A CONEXAO COM O SERVIDOR
		paraServidor.writeUTF("" + 1234);
		// Fecha o socket e as streams
		scan.close();
		paraServidor.close();
		doServidor.close();
		socketCliente.close();
	}

	public static void enviaArquivo(DataOutputStream paraServidor, File file) throws IOException {
		InputStream in = new FileInputStream(file);
		paraServidor.writeUTF(file.getName());
		byte[] bytes = new byte[16 * 1024];
		int count;
		while ((count = in.read(bytes)) > 0) {
			paraServidor.write(bytes, 0, count);
		}
		// Envia o caracter para sinalizar que terminou de enviar o arquivo
		paraServidor.write('#');
		in.close();
	}

	private static void recebeArquivo(Socket socketCliente) throws IOException {
		InputStream doServidor = socketCliente.getInputStream();
		File arquivo = selectFile();
		OutputStream out = new FileOutputStream(arquivo);
		byte[] bytes = new byte[16 * 1024];
		int count;
		while ((count = doServidor.read(bytes)) > 0) {
			System.out.println(count);
			if (count == 1 && bytes[0] == '#') {
				break;
			} 
			if(bytes[count-1] == '#') {
				break;
			}
			else {
				out.write(bytes, 0, count);
			}
		}
		System.out.println("Arquivo recebido do servidor!");
		out.close();
	}

	public static File selectFile() throws FileNotFoundException {

		JFileChooser fileChooser = new JFileChooser();

		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			System.out.println(filename);
			File file = new File(filename);
			// Get the size of the file
			return file;
		} else if (result == JFileChooser.CANCEL_OPTION) {
			JOptionPane.showMessageDialog(null, "Nenhum arquivo foi selecionado!");
		} else if (result == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(null, "Erro ao selecionar o arquivo!");
		}
		return null;
	}

	private static int menu() {
            String[] opcoes = {"Enviar Arquivo para o servidor", "Baixar arquivo do servidor", "Sair"};
        return JOptionPane.showOptionDialog(null, "Selecione uma opção",
                "Gnome Drive",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);
	}
}
