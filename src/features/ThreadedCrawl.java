package features;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * O programa main () nesta classe testa a classe ThreadImageCrawler.
 * Ele apenas cria um ThreadedImageCrawler, fornece um URL inicial e
 * obtém um URL de imagem do rastreador a cada três segundos em um 
 * loop infinito até o programa terminar.
 */
public class ThreadedCrawl {

	private static final int CONTADOR_THREAD = 5;  // número de threads no crawler

	public static void main(String[] args) {

		Scanner stdin = new Scanner(System.in);  // Para ler a entrada do usuário.
		
		String stringURL;   // URL inicial, do usuário ou arg [0].
		if (args.length > 0) {
			stringURL = args[0];
		}
		else{
			System.out.print("Digite o URL de início:  ");
			stringURL = stdin.nextLine();
		}
		URL startURL;
		try {
			startURL = new URL(stringURL);
		}
		catch (MalformedURLException e) {
			System.out.println("Desculpe, \"" + stringURL + "\" não é uma URL valida.");
			return;
		}

		ThreadedImageCrawler crawler = new ThreadedImageCrawler(CONTADOR_THREAD);
		
		crawler.adicionarURLFila(startURL);
		
		while (true) {
			URL url = crawler.getProximaURLDeImagemDisponivel();
			if (url == null) {
				System.out.println("**** Nenhum URL de imagem disponível no momento.");
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
