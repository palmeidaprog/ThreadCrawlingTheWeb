package features;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class GetURL {

	public static void main(String[] args) {
		
		Scanner stdin = new Scanner(System.in);

		String stringURL;
		
		if (args.length > 0)
			stringURL = args[0];
		else{
			System.out.print("Digite o URL para fazer o download:  ");
			stringURL = stdin.nextLine();
		}
		
		URL url;
		try {
			url = new URL(stringURL);
		}
		catch (MalformedURLException e) {
			System.out.println("Desculpe, \"" + stringURL + "\" não é uma URL válida.");
			return;
		}
		
		URLConnection connection;
		InputStream in;
		try {
			System.out.println("Conectando ...");
			connection = url.openConnection();
			in = connection.getInputStream();
			System.out.println(" Conectado.\n");
		}
		catch (Exception e) {
			System.out.println("Desculpe, não é possível conectar-se a \" " + stringURL + "\".");
			return;
		}

		try {
			String tipoConteudo = connection.getContentType();
			if (tipoConteudo == null) {
				System.out.println("Desculpe, não consigo descobrir o tipo de dados.");
			}
			else if (tipoConteudo.startsWith("text")) {
				saveText(in);
			}
			else if (tipoConteudo.startsWith("image/")) {
				saveImage(in, tipoConteudo.substring(6).toUpperCase());
			}
			else {
				System.out.println("\"Desculpe, não entendo o tipo de conteúdo \"" + tipoConteudo + "\".");
			}
			System.out.println("Feito!");
		}
		finally {
			try{
				in.close();
			}
			catch (IOException e) {
			}
		}
		
	}

	private static void saveImage(InputStream source, String nomeFormato) {
		try {
			System.out.println("Recuperando arquivo de imagem ...");
			BufferedImage image = ImageIO.read(source);
			if (image == null)
				throw new Exception("Não foi possível reconhecer o formato da imagem.");
			File arquivoOutPut = new File("imagem1." + nomeFormato.toLowerCase());
			int ct = 1;
			while ( arquivoOutPut.exists() ) {
				arquivoOutPut = new File("imagem2" + ct + "." + nomeFormato.toLowerCase());
				ct++;
			}
			System.out.println("Gravando imagem no arquivo " + arquivoOutPut.getAbsolutePath() + "...");
			if (ImageIO.write(image, nomeFormato, arquivoOutPut) == false) {
				System.out.println("Formato " + nomeFormato + " não suportado.  Tente JPEG");
				arquivoOutPut = new File("imagem1.jpeg");
				ct = 1;
				while ( arquivoOutPut.exists() ) {
					arquivoOutPut = new File("imagem2" + ct + ".jpeg");
					ct++;
				}
				System.out.println("Gravando imagem no arquivo " + arquivoOutPut.getAbsolutePath() + "...");
				if (ImageIO.write(image,"JPEG", arquivoOutPut) == false)
					throw new Exception("Formato JPEG não suportado???");
			}
		}
		catch (Exception e) {
			System.out.println("Desculpe, ocorreu um erro ao salvar o arquivo de imagem:");
			System.out.println(e);
		}
	}

	private static void saveText(InputStream source) {	
		try {
			System.out.println("Retrieving text file...");
			Scanner in = new Scanner(source);
			File outputFile = new File("saved_text_file.txt");
			int ct = 1;
			while ( outputFile.exists() ) {
				outputFile = new File("saved_text_file_" + ct + ".txt");
				ct++;
			}
			PrintWriter out = new PrintWriter(outputFile);
			while (in.hasNextLine()) {
				out.println(in.nextLine());
			}
			out.flush();
			if (out.checkError())
				throw new Exception("Some error occurred while writing to the file.");
		}
		catch (Exception e) {
			System.out.println("Sorry, an error occurred while saving the text file:");
			System.out.println(e);
		}
	}

}