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
* URLs de imagem. O método {@link #addURLBook (URL)} deve ser chamado
* pelo menos uma vez para fornecer ao rastreador uma página da web inicial. Vai baixar
* nessa página e procure por URLs de imagem em tags IMG. Coloca a imagem
* URLs em uma fila. É possível obter imagens da fila chamando
* {@link #getNextImageURL ()} ou {@link #getProximaImageAvailable ()}.
* O crawler também procura links na página da Web nas tags A e adiciona o
* links encontrados para uma fila de URLs. Os URLs são removidos continuamente de
* Essa fila é pesquisada da mesma maneira. O rastreamento é feito por
* vários threads.
* <p> Observe que a interface pública da classe é pequena e simples!
* <p> Observe que o crawler é "acelerado" pelo fato de a fila de imagens
* é de tamanho limitado. Depois de preenchido, os segmentos do crawler bloquearão
* até que haja espaço na fila. Depois disso, os threads parecerão apenas
* para obter mais imagens, conforme necessário, para manter a fila de imagens cheia.
* <p> Limitações: o tamanho da fila da URL é limitado. Quando estiver cheio,
* novos URLs encontrados são simplesmente descartados. Isso é para evitar um deadlock
* que pode ocorrer se todos as thread de crawler acessar ao mesmo tempo. Talvez seja melhor deixar
* a fila cresce para um tamanho arbitrário, ou pelo menos para deixá-la crescer muito
* tamanho grande. O método para determinar o URL base de uma página da web é
* incorreto - pressupõe-se que o URL base seja o URL usado
* para acessar a página.
*/
public class ThreadedImageCrawler {
	
	/**
	 * Um atraso, em milissegundos, que é inserido por uma thread de crawler
	 * depois de baixar uma página da web. Isso é para ajudar a evitar inundações
	 * A rede terá um fluxo contínuo de solicitações.
	 */
	private final static int DALAY_PARA_EVITAR_FLOODING = 500;
	
	/**
	 * Crie um crawler que use um número especificado de threads. Usa um URL
	 * fila de comprimento 250 e uma fila de imagem de comprimento 50.
	 * @param threadPoolSize o número de threads de crawler a serem criados; devemos ser
	 * positivo
	 * @throws IllegalArgumentException se o threadPoolSize for 0 ou menos.
	 */
	public ThreadedImageCrawler(int threadPoolSize) {
		this(threadPoolSize,250,50);
	}
	
	/**
	 * Crie um crawler com propriedades especificadas.
	 * @param threadPoolSize o número de threads do crawler que serão usados.
	 * @param maxTamanhoFilaURL o tamanho máximo da fila de URL. Quando isso
	 * a fila é preenchida, novos URLs encontrados são jogados fora.
	 * @param maxTamanhoFilaImagem o tamanho máximo da fila de imagens. Quando
	 * essa fila é preenchida e uma thread de crawler deseja adicionar uma nova imagem ao
	 * a fila, essa thread está bloqueado. Ele irá bloquear até que a sala se torne
	 * disponível na fila de imagens, pois as imagens são removidas da fila.
	 * @throws IllegalArgumentException se threadPoolSize for menor que 1 ou
	 * qualquer um dos tamanhos da fila é menor que 10.
	 */
	public ThreadedImageCrawler(int threadPoolSize, 
			int maxTamanhoFilaURL, int maxTamanhoFilaImagem) {
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException(
					"O numero de thread deve ser maior que zero.");
		}
		if (maxTamanhoFilaURL < 10 || maxTamanhoFilaImagem < 10) {
			throw new IllegalArgumentException(
					"O tamanho maáximo da fila deve ser pelo o menos 10.");
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
	 * Adicione um endereço à fila da URL. Este URL será usado como um
	 * ponto de partida para crawlling a web. Se a fila de URL estiver cheia,
	 * este método será bloqueado até que haja espaço disponível. 
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
	 * Obtenha a próxima imagem disponível na fila de imagens. Se o
	 * fila está vazia, este método retorna nulo. Não bloqueia.
	 */
	public URL getProximaURLDeImagemDisponivel() {
		return imageURLFila.poll();
	}
	
	/**
	 * Obtenha a próxima imagem disponível na fila de imagens. Se o
	 * fila está vazia, este método será bloqueado até que uma imagem se torne
	 * acessível.
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
	
// ------------- o restante da classe é a implementação privada ---------
	
    /*
     * Três padrões para usar na busca de links nas páginas da web.
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
		 * Envie uma mensagem para um log. Não faz nada, já que o comando de saída em
		 * este método foi comentado. Remova o comentário para ver as mensagens de log.
		 */
		private void log(String mensagem) {
//			System.out.println("Mesagem de " + id + ": " + mensagem);
		}

		/*
		 * O método run executa um loop infinito no qual uma URL é
		 * removido da fila de URLs e processado. O processamento consiste
		 * de conexão com o URL e pesquisando a página HTML para
		 * links para imagens e outras páginas da web, se possível. Imagem
		 * links são adicionados à fila de imagens. Outros links são "oferecidos"
		 * para a fila da URL.
		 */
		public void run() {
			ArrayList<URL> linkURLs = new ArrayList<URL>();
			ArrayList<URL> imagemURL = new ArrayList<URL>();
			while (true) {
				URL url;
				try {
					url = urlFila.take(); // obtém o URL da fila; pode bloquear.
				}
				catch (InterruptedException e) {
					continue;  // volta ao início do while loop; provavelmente não pode acontecer
				}
				InputStream conteudoDaPagina = getConnection(url,imagemURL);
				if (conteudoDaPagina == null)
					continue;  // nenhuma página HTML; voltar ao início do loop while
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
		 * Abra uma conexão com um URL. Se a conexão for bem-sucedida e o
		 * o tipo de conteúdo do recurso é html, então um fluxo de entrada é
		 * aberto para ler o conteúdo. Se o conteúdo for uma imagem,
		 * o URL é adicionado à lista de imageURLs para que ele possa
		 * ser adicionado à fila de imagens.
		 */
		private InputStream getConnection(URL url, ArrayList<URL> imageURLs) {
			InputStream paginaConteudo;
			URLConnection connection;
			try {
				connection = url.openConnection();
				paginaConteudo = connection.getInputStream();
			}
			catch (Exception e) {
				log("Não pode se conectar à " + url);
				return null;
			}
			String tipoConteudo = connection.getContentType();
			if (tipoConteudo != null && (tipoConteudo.startsWith("text/html") ||
					tipoConteudo.startsWith("application/xhtml+xml"))) {
				return paginaConteudo;
			}
			if (tipoConteudo == null) {
				log("Não é possível descobrir o tipo de dados de" + url);
			}
			else if (tipoConteudo.startsWith("image/")) {
				imageURLs.add(url);
			}
			else {
				log("Não é possível lidar com o tipo de conteúdo" + tipoConteudo+ " da " + url);
			}
			try {
				paginaConteudo.close();
			}
			catch (IOException e) {
			}
			return null;
		}

		/**
		 * Lê a partir de um fluxo de imput, que será texto HTML e
		 * procura por links de imagens e links para outras páginas da web. o
		 * URLs encontrados são adicionados ao ArrayList apropriado.
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
						catch (MalformedURLException e) { // url ruim é ignorada
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
					in.close(); // fecha o fluxo, ignorando qualquer exceção
				}
				catch (Exception e) {
				}
			}
		}
		
	} // fim do aninhamento de CrawlThread
	
	
}