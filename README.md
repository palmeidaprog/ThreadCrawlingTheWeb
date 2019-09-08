# Web Crawler de imagens

Desenvolver um Web Crawler que extraia todas as URLs de imagens de de sites armazendos em um arquivo . Cada site do arquivo deve ser tratado por uma Thread.

As URLs das imagens de cada página devem ser colocadas em um buffer comopartilhado.

Deve existir uma classe (ou equivalente em sua linguagem) responsável por fazer o download de cada imagem, lendo do buffer compartilhado a URL e realizando o download em uma thread por URL/imagem.
