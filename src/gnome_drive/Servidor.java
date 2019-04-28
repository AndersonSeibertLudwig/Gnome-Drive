package gnome_drive;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Servidor extends Thread {
	public final static int FILE_SIZE = Integer.MAX_VALUE;
	Socket socketCliente;
	static ArrayList<File> arquivos = new ArrayList<File>();
	static int numCliente = 0;

	public Servidor(Socket cliente) {
		this.socketCliente = cliente;
	}

	public static void main(String argv[]) throws Exception {
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
				opcao = Integer.parseInt(doCliente.readUTF());
				switch (opcao) {
				case 1:
						recebeArquivo(doCliente);
					break;
				case 2:
					if (arquivos.isEmpty()) {
						paraCliente.writeUTF("null");
					} else {
						String nomeArquivos = "";
						for (int i = 0; i < arquivos.size(); i++) {
							nomeArquivos += arquivos.get(i).getName() + ";";
						}
						paraCliente.writeUTF(nomeArquivos);
						int opcao2 = Integer.parseInt(doCliente.readUTF());
						System.out.println(opcao2);
						if (opcao2 > 0 && opcao2 <= arquivos.size()) {
							enviaArquivo(paraCliente, opcao2 - 1);
						}
					}
					break;
				default:

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

	public static void enviaArquivo(DataOutputStream paraCliente, int numArquivo) throws IOException {
		InputStream in = new FileInputStream(arquivos.get(numArquivo));
		byte[] bytes = new byte[16 * 1024];
		int count;
		System.out.println("Enviando arquivo para o cliente...");
		while ((count = in.read(bytes)) > 0) {
			paraCliente.write(bytes, 0, count);
		}
		// Envia o caracter para sinalizar que terminou de enviar o arquivo
		paraCliente.write('#');
		in.close();
		System.out.println("Enviado!");
	}

	private void recebeArquivo(DataInputStream input) throws IOException {
		InputStream doCliente = socketCliente.getInputStream();
		arquivos.add(new File(input.readUTF()));
		OutputStream out = new FileOutputStream(arquivos.get(arquivos.size() - 1));
		byte[] bytes = new byte[16 * 1024];
		int count;
		while ((count = doCliente.read(bytes)) > 0) {
			if (count == 1 && bytes[0] == '#') {
				System.out.println("Arquivo recebido do cliente!");
				break;
			}
			else if (bytes[count - 1] == '#') {
				System.out.println("Arquivo recebido do cliente!");
				break;
			} else {
				out.write(bytes, 0, count);
			}
		}
		out.close();
	}

}