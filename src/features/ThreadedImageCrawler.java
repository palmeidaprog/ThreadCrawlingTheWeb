package features;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* Esta classe representa um "rastreador da web" que rastreia a web procurando
* URLs de imagem. O m�todo {@link #addURLBook (URL)} deve ser chamado
* pelo menos uma vez para fornecer ao rastreador uma p�gina da web inicial. Vai baixar
* nessa p�gina e procure por URLs de imagem em tags IMG. Coloca a imagem
* URLs em uma fila. � poss�vel obter imagens da fila chamando
* {@link #getNextImageURL ()} ou {@link #getProximaImageAvailable ()}.
* O crawler tamb�m procura links na p�gina da Web nas tags A e adiciona o
* links encontrados para uma fila de URLs. Os URLs s�o removidos continuamente de
* Essa fila � pesquisada da mesma maneira. O rastreamento � feito por
* v�rios threads.
* <p> Observe que a interface p�blica da classe � pequena e simples!
* <p> Observe que o crawler � "acelerado" pelo fato de a fila de imagens
* � de tamanho limitado. Depois de preenchido, os segmentos do crawler bloquear�o
* at� que haja espa�o na fila. Depois disso, os threads parecer�o apenas
* para obter mais imagens, conforme necess�rio, para manter a fila de imagens cheia.
* <p> Limita��es: o tamanho da fila da URL � limitado. Quando estiver cheio,
* novos URLs encontrados s�o simplesmente descartados. Isso � para evitar um deadlock
* que pode ocorrer se todos as thread de crawler acessar ao mesmo tempo. Talvez seja melhor deixar
* a fila cresce para um tamanho arbitr�rio, ou pelo menos para deix�-la crescer muito
* tamanho grande. O m�todo para determinar o URL base de uma p�gina da web �
* incorreto - pressup�e-se que o URL base seja o URL usado
* para acessar a p�gina.
*/
public class ThreadedImageCrawler {
	
	/**
	 * Um atraso, em milissegundos, que � inserido por uma thread de crawler
	 * depois de baixar uma p�gina da web. Isso � para ajudar a evitar inunda��es
	 * A rede ter� um fluxo cont�nuo de solicita��es.
	 */
	private final static int DALAY_PARA_EVITAR_FLOODING = 500;
	
	/**
	 * Crie um crawler que use um n�mero especificado de threads. Usa um URL
	 * fila de comprimento 250 e uma fila de imagem de comprimento 50.
	 * @param threadPoolSize o n�mero de threads de crawler a serem criados; devemos ser
	 * positivo
	 * @throws IllegalArgumentException se o threadPoolSize for 0 ou menos.
	 */
	public ThreadedImageCrawler(int threadPoolSize) {
		this(threadPoolSize,250,50);
	}
	
	/**
	 * Crie um crawler com propriedades especificadas.
	 * @param threadPoolSize o n�mero de threads do crawler que ser�o usados.
	 * @param maxTamanhoFilaURL o tamanho m�ximo da fila de URL. Quando isso
	 * a fila � preenchida, novos URLs encontrados s�o jogados fora.
	 * @param maxTamanhoFilaImagem o tamanho m�ximo da fila de imagens. Quando
	 * essa fila � preenchida e uma thread de crawler deseja adicionar uma nova imagem ao
	 * a fila, essa thread est� bloqueado. Ele ir� bloquear at� que a sala se torne
	 * dispon�vel na fila de imagens, pois as imagens s�o removidas da fila.
	 * @throws IllegalArgumentException se threadPoolSize for menor que 1 ou
	 * qualquer um dos tamanhos da fila � menor que 10.
	 */
	public ThreadedImageCrawler(int threadPoolSize, 
			int maxTamanhoFilaURL, int maxTamanhoFilaImagem) {
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException(
					"O numero de thread deve ser maior que zero.");
		}
		if (maxTamanhoFilaURL < 10 || maxTamanhoFilaImagem < 10) {
			throw new IllegalArgumentException(
					"O tamanho ma�ximo da fila deve ser pelo o menos 10.");
		}
		threadPool = new CrawlThread[threadPoolSize];
		urlsEncontradas = Collections.synchronizedSet(new HashSet<URL>());
		urlFila = new ArrayBlockingQueue<URL>(maxTamanhoFilaURL);
		imageURLFila = new ArrayBlockingQueue<URL>(maxTamanhoFilaImagem);
		for (int i = 0; i < threadPoolSize; i++) {
			threadPool[i] = new CrawlThread(i);
			threadPool[i].start();
		}
	}
	
	/**
	 * Adicione um endere�o � fila da URL. Este URL ser� usado como um
	 * ponto de partida para crawlling a web. Se a fila de URL estiver cheia,
	 * este m�todo ser� bloqueado at� que haja espa�o dispon�vel. 
	 */
	public void adicionarURLFila(URL url) {
		while (true) {
			try {
				urlFila.put(url);
				return;
			}
			catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Obtenha a pr�xima imagem dispon�vel na fila de imagens. Se o
	 * fila est� vazia, este m�todo retorna nulo. N�o bloqueia.
	 */
	public URL getProximaURLDeImagemDisponivel() {
		return imageURLFila.poll();
	}
	
	/**
	 * Obtenha a pr�xima imagem dispon�vel na fila de imagens. Se o
	 * fila est� vazia, este m�todo ser� bloqueado at� que uma imagem se torne
	 * acess�vel.
	 */
	public URL getNextImageURL() {
		while (true) {
			try {
				return imageURLFila.take();
			}
			catch (InterruptedException e) {
			}
		}
	}
	
// ------------- o restante da classe � a implementa��o privada ---------
	
    /*
     * Tr�s padr�es para usar na busca de links nas p�ginas da web.
     */
	private final static Pattern padraoWebLink = Pattern.compile(
			"<a [^>]*href\\s*=\\s*(\"[^\"]+\"|'[^']+')", 
			Pattern.CASE_INSENSITIVE);
	private final static Pattern padraoFrameLink = Pattern.compile(
			"<frame [^>]*src\\s*=\\s*(\"[^\"]+\"|'[^']+')", 
			Pattern.CASE_INSENSITIVE);
	private final static Pattern padraoImageLink = Pattern.compile(
			"<img [^>]*src\\s*=\\s*(\"[^\"]+\"|'[^']+')", 
			Pattern.CASE_INSENSITIVE);

	private Set<URL> urlsEncontradas; // Use para evitar a visita a URLs duplicados
	private ArrayBlockingQueue<URL> urlFila;  // fila de URL do rastreador
	private ArrayBlockingQueue<URL> imageURLFila;  // a fila da imagem
	private CrawlThread[] threadPool;  // as thread crawler
	
	/**
	 * Uma subclasse de Thread que define os threads que crawl a web.
	 */
	private class CrawlThread extends Thread {
		
		private int id; // An id, used only in log messages

		CrawlThread(int id) {
			setDaemon(true);
			setPriority(getPriority() - 1);
			this.id = id;
		}
		
		/**
		 * Envie uma mensagem para um log. N�o faz nada, j� que o comando de sa�da em
		 * este m�todo foi comentado. Remova o coment�rio para ver as mensagens de log.
		 */
		private void log(String mensagem) {
//			System.out.println("Mesagem de " + id + ": " + mensagem);
		}

		/*
		 * O m�todo run executa um loop infinito no qual uma URL �
		 * removido da fila de URLs e processado. O processamento consiste
		 * de conex�o com o URL e pesquisando a p�gina HTML para
		 * links para imagens e outras p�ginas da web, se poss�vel. Imagem
		 * links s�o adicionados � fila de imagens. Outros links s�o "oferecidos"
		 * para a fila da URL.
		 */
		public void run() {
			ArrayList<URL> linkURLs = new ArrayList<URL>();
			ArrayList<URL> imagemURL = new ArrayList<URL>();
			while (true) {
				URL url;
				try {
					url = urlFila.take(); // obt�m o URL da fila; pode bloquear.
				}
				catch (InterruptedException e) {
					continue;  // volta ao in�cio do while loop; provavelmente n�o pode acontecer
				}
				InputStream conteudoDaPagina = getConnection(url,imagemURL);
				if (conteudoDaPagina == null)
					continue;  // nenhuma p�gina HTML; voltar ao in�cio do loop while
				getURLs(conteudoDaPagina, url, linkURLs, imagemURL);
				int imageCt = 0;
				for (URL enderecoImagem : imagemURL) {
					if (urlsEncontradas.add(enderecoImagem)) {
						try {
							imageURLFila.put(enderecoImagem);
							log("Adicionado " + enderecoImagem + " a fila de imagem.");
							imageCt++;
						}
						catch (InterruptedException e) {
						}
					}
					log("Adicionado " + enderecoImagem + " a fila de imagem.");
				}
				for (URL enderecoDoLink : linkURLs) {
					if (urlsEncontradas.add(enderecoDoLink)) {
						if (urlFila.offer(enderecoDoLink) == false)
							break;
					}
				}
				linkURLs.clear();
				imagemURL.clear();
				try {
					Thread.sleep(DALAY_PARA_EVITAR_FLOODING);
				}
				catch (InterruptedException e) {
				}
			}
		}

		/**
		 * Abra uma conex�o com um URL. Se a conex�o for bem-sucedida e o
		 * o tipo de conte�do do recurso � html, ent�o um fluxo de entrada �
		 * aberto para ler o conte�do. Se o conte�do for uma imagem,
		 * o URL � adicionado � lista de imageURLs para que ele possa
		 * ser adicionado � fila de imagens.
		 */
		private InputStream getConnection(URL url, ArrayList<URL> imageURLs) {
			InputStream paginaConteudo;
			URLConnection connection;
			try {
				connection = url.openConnection();
				paginaConteudo = connection.getInputStream();
			}
			catch (Exception e) {
				log("N�o pode se conectar � " + url);
				return null;
			}
			String tipoConteudo = connection.getContentType();
			if (tipoConteudo != null && (tipoConteudo.startsWith("text/html") ||
					tipoConteudo.startsWith("application/xhtml+xml"))) {
				return paginaConteudo;
			}
			if (tipoConteudo == null) {
				log("N�o � poss�vel descobrir o tipo de dados de" + url);
			}
			else if (tipoConteudo.startsWith("image/")) {
				imageURLs.add(url);
			}
			else {
				log("N�o � poss�vel lidar com o tipo de conte�do" + tipoConteudo+ " da " + url);
			}
			try {
				paginaConteudo.close();
			}
			catch (IOException e) {
			}
			return null;
		}

		/**
		 * L� a partir de um fluxo de imput, que ser� texto HTML e
		 * procura por links de imagens e links para outras p�ginas da web. o
		 * URLs encontrados s�o adicionados ao ArrayList apropriado.
		 */
		private void getURLs(InputStream origem, URL urlPai,
				ArrayList<URL> linkURLs, ArrayList<URL> imageURLs) {
			Scanner in = new Scanner(origem);
			try {
				while (in.hasNextLine()) {
					String linha = in.nextLine();
					Matcher matcher;
					matcher = padraoWebLink.matcher(linha);
					while ( matcher.find() ) { // achou a URL com "a" tag
						String endereco = matcher.group(1);
						endereco = endereco.substring(1,endereco.length()-1);
						try {
							linkURLs.add(new URL(urlPai,endereco));
						}
						catch (MalformedURLException e) { // url ruim � ignorada
						}
					}
					matcher = padraoFrameLink.matcher(linha);
					while ( matcher.find() ) { // achou uma url com "framte" tag
						String endereco = matcher.group(1);
						endereco = endereco.substring(1,endereco.length()-1);
						try {
							linkURLs.add(new URL(urlPai,endereco));
						}
						catch (MalformedURLException e) {
						}
					}
					matcher = padraoImageLink.matcher(linha);
					while ( matcher.find() ) { // achou uma url com "framte" tag
						String address = matcher.group(1);
						address = address.substring(1,address.length()-1);
						try {
							imageURLs.add(new URL(urlPai,address));
						}
						catch (MalformedURLException e) {
						}
					}
				}
			}
			catch (Exception e) {
			}
			finally {
				try {
					in.close(); // fecha o fluxo, ignorando qualquer exce��o
				}
				catch (Exception e) {
				}
			}
		}
		
	} // fim do aninhamento de CrawlThread
	
	
}