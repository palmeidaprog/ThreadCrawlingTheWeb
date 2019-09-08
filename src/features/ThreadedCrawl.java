package features;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * O programa main () nesta classe testa a classe ThreadImageCrawler.
 * Ele apenas cria um ThreadedImageCrawler, fornece um URL inicial e
 * obt�m um URL de imagem do rastreador a cada tr�s segundos em um 
 * loop infinito at� o programa terminar.
 */
public class ThreadedCrawl {

	private static final int CONTADOR_THREAD = 5;  // n�mero de threads no crawler

	public static void main(String[] args) {

		Scanner stdin = new Scanner(System.in);  // Para ler a entrada do usu�rio.
		
		String stringURL;   // URL inicial, do usu�rio ou arg [0].
		if (args.length > 0) {
			stringURL = args[0];
		}
		else{
			System.out.print("Digite o URL de in�cio:  ");
			stringURL = stdin.nextLine();
		}
		URL startURL;
		try {
			startURL = new URL(stringURL);
		}
		catch (MalformedURLException e) {
			System.out.println("Desculpe, \"" + stringURL + "\" n�o � uma URL valida.");
			return;
		}

		ThreadedImageCrawler crawler = new ThreadedImageCrawler(CONTADOR_THREAD);
		
		crawler.adicionarURLFila(startURL);
		
		while (true) {
			URL url = crawler.getProximaURLDeImagemDisponivel();
			if (url == null) {
				System.out.println("**** Nenhum URL de imagem dispon�vel no momento.");
			}
			else {
				System.out.println("**** Obteve " + url + " da  fila de imagem");
			}
			try {
				Thread.sleep(3000); // pause 3 seconds
			}
			catch (InterruptedException e) {
			}
		}


	}

}
