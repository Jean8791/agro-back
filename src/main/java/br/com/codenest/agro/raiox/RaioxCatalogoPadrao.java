package br.com.codenest.agro.raiox;

import br.com.codenest.agro.raiox.checklist.enums.ModuloChecklist;
import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import java.util.List;

/**
 * Catálogo padrão do Raio-X: os 75 produtos/atividades e o checklist de
 * custos originais da planilha "Mini Raio-X do Preço Mínimo"
 * (mini_raio_x_preco_minimo_V1_QA.xlsx, sheets 02_BASE_UNIDADES e
 * 04_CHECKLIST_CUSTOS). Fonte única usada tanto por
 * {@code ProdutorService.criar()} (semear produtor novo) quanto pelo
 * endpoint {@code POST /raiox/produtos-base/semear-padrao} (backfill /
 * restaurar), pra não haver duas cópias divergindo com o tempo.
 */
public final class RaioxCatalogoPadrao {

    public record ProdutoPadrao(
            ModuloProducao modulo,
            String categoria,
            String produto,
            String unidadeTecnica,
            String unidadeComercial,
            double quantidadePorUnidade,
            String unidadePreco,
            TipoProducaoAnimal tipoProducaoAnimal,
            String nota,
            String fonte) {
    }

    public record ChecklistPadrao(ModuloChecklist modulo, String rotulo, String nota) {
    }

    public static final List<ProdutoPadrao> PRODUTOS = List.of(
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Milho grão", "kg", "saca", 60.0, "R$/saca", null, "Padrão nacional de saca 60 kg; pode variar no contrato.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Feijão", "kg", "saca", 60.0, "R$/saca", null, "Usar preço líquido recebido; considerar frete e classificação.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Arroz em casca", "kg", "saca", 50.0, "R$/saca", null, "Arroz em casca geralmente referenciado em saca 50 kg.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Café", "kg", "saca", 60.0, "R$/saca", null, "Café geralmente em saca 60 kg; qualidade altera preço.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz/agroindústria", "Mandioca indústria", "kg", "tonelada", 1000.0, "R$/tonelada", null, "Raiz posta na indústria/fecularia normalmente em tonelada.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz/mesa", "Mandioca de mesa", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Caixa ou kg varia por praça; ajustar peso real.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Agroindústria", "Cana-de-açúcar", "kg", "tonelada", 1000.0, "R$/tonelada", null, "Usar preço líquido por tonelada entregue.", "https://portaldeinformacoes.conab.gov.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Amendoim", "kg", "saca", 25.0, "R$/saca", null, "Saca pode variar; ajustar conforme comprador.", "https://portaldeinformacoes.conab.gov.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Trigo", "kg", "saca", 60.0, "R$/saca", null, "Saca 60 kg como referência comum.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Soja", "kg", "saca", 60.0, "R$/saca", null, "Saca 60 kg como referência comum.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Grão", "Sorgo", "kg", "saca", 60.0, "R$/saca", null, "Saca 60 kg como referência comum.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Alface", "cabeça", "caixa", 20.0, "R$/caixa ou R$/cabeça", null, "Pode ser cabeça/pé, caixa 12/18/20/24/32/36 ou kg conforme praça.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Couve-manteiga", "maço", "caixa", 24.0, "R$/maço", null, "Comum em maço; caixa deve ser convertida para maços.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Rúcula", "maço", "maço", 1.0, "R$/maço", null, "Pode ser maço ou unidade hidropônica; ajustar canal.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Agrião", "maço", "maço", 1.0, "R$/maço", null, "Maço é referência prática para pequeno produtor.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Cheiro-verde", "maço", "maço", 1.0, "R$/maço", null, "Maço varia por região; usar padrão do comprador.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Coentro", "maço", "maço", 1.0, "R$/maço", null, "Maço varia por região; usar padrão do comprador.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Salsa", "maço", "maço", 1.0, "R$/maço", null, "Maço varia por região; usar padrão do comprador.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Almeirão", "maço", "maço", 1.0, "R$/maço", null, "Pode aparecer por tipo/embalagem em CEASA.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Espinafre", "maço", "maço", 1.0, "R$/maço", null, "Pode ser maço ou caixa com maços.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Folhosa", "Chicória/Escarola", "unidade", "caixa", 24.0, "R$/unidade", null, "Converter caixa para unidades conforme classificação.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Tomate", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Caixa 20 kg é referência comum em atacado.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça bulbo", "Cebola", "kg", "saco", 20.0, "R$/saco ou R$/kg", null, "Saco 20 kg como referência em CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz", "Cenoura", "kg", "saco/caixa", 20.0, "R$/saco ou R$/kg", null, "Papelão/saco 20 kg em referência de atacado.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz", "Beterraba", "kg", "caixa/saco", 20.0, "R$/caixa ou R$/kg", null, "Ajustar peso real da embalagem local.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Tubérculo", "Batata inglesa", "kg", "saco", 50.0, "R$/saco ou R$/kg", null, "Saco pode ser 25 ou 50 kg; editar conforme praça.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Tubérculo", "Batata-doce", "kg", "caixa/saco", 20.0, "R$/caixa ou R$/kg", null, "Peso da embalagem varia; editar.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Abóbora", "kg", "kg", 1.0, "R$/kg", null, "Frequentemente vendida por kg ou granel.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Abobrinha", "kg", "caixa", 18.0, "R$/caixa ou R$/kg", null, "Caixa 18 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Berinjela", "kg", "caixa", 10.0, "R$/caixa ou R$/kg", null, "Caixa/papelão 10 kg em referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Pepino", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Caixa 20 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Pimentão", "kg", "caixa", 10.0, "R$/caixa ou R$/kg", null, "Papelão 10 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Quiabo", "kg", "caixa", 15.0, "R$/caixa ou R$/kg", null, "Caixa 15 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Vagem", "kg", "caixa", 15.0, "R$/caixa ou R$/kg", null, "Caixa 15 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Chuchu", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Caixa 20 kg como referência prática.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça fruto", "Jiló", "kg", "caixa", 15.0, "R$/caixa ou R$/kg", null, "Editar peso conforme comprador.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça cabeça", "Repolho", "cabeça", "caixa", 12.0, "R$/cabeça ou R$/caixa", null, "Converter caixa para cabeças; peso varia por classificação.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça cabeça", "Brócolis ninja", "cabeça", "caixa", 8.0, "R$/cabeça", null, "Pode variar por tipo 6/8/10 unidades.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça maço", "Brócolis ramoso", "maço", "caixa", 12.0, "R$/maço", null, "Converter caixa para maços.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Hortaliça cabeça", "Couve-flor", "cabeça", "caixa", 8.0, "R$/cabeça", null, "Pode variar por classificação 6/8/10.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Espiga", "Milho verde", "espiga", "saco", 50.0, "R$/espiga ou R$/saco", null, "Saco pode ser por peso ou número de espigas; editar.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz/tubérculo", "Inhame/Taro", "kg", "caixa", 18.0, "R$/caixa ou R$/kg", null, "Editar peso local da caixa.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz/tubérculo", "Cará", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Editar peso local da caixa.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Raiz/tubérculo", "Mandioquinha", "kg", "caixa", 17.0, "R$/caixa ou R$/kg", null, "Editar conforme praça.", "https://ceagesp.gov.br/wp-content/uploads/2021/01/medidaHortalicas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Banana", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Caixa 20 kg como referência CEAGESP.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Abacaxi", "unidade", "unidade", 1.0, "R$/unidade", null, "Pode ser unidade ou caixa por quantidade; usar unidade facilita.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Laranja", "kg", "caixa", 25.0, "R$/caixa ou R$/kg", null, "Caixa 25 kg como referência de atacado.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Limão Tahiti", "kg", "caixa", 25.0, "R$/caixa ou R$/kg", null, "Peso pode variar 20 ou 25 kg; editar.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Mamão Formosa", "kg", "caixa", 10.0, "R$/caixa ou R$/kg", null, "Caixa 10 kg como referência.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Mamão Havaí", "kg", "caixa", 8.0, "R$/caixa ou R$/kg", null, "Caixa 8 kg como referência.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Manga", "kg", "caixa", 6.0, "R$/caixa ou R$/kg", null, "Caixa 6 kg como referência.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Maracujá", "kg", "caixa", 13.0, "R$/caixa ou R$/kg", null, "Caixa 13 kg como referência.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Melancia", "kg", "kg", 1.0, "R$/kg", null, "Geralmente granel/kg; unidade exige peso médio.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Melão", "kg", "caixa", 13.0, "R$/caixa ou R$/kg", null, "Caixa 13 kg como referência.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Goiaba", "kg", "caixa", 2.0, "R$/caixa ou R$/kg", null, "Peso varia por embalagem; editar.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Coco verde", "unidade", "unidade", 1.0, "R$/unidade", null, "Comum por unidade.", "https://ceagesp.gov.br/cotacoes/"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Morango", "bandeja", "bandeja", 1.0, "R$/bandeja ou R$/kg", null, "Bandeja/cumbuca varia por peso; informar padrão do comprador.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Uva", "kg", "kg", 1.0, "R$/kg", null, "Pode ser caixa ou bandeja; ajustar conforme canal.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Acerola", "kg", "caixa", 1.2, "R$/caixa ou R$/kg", null, "Peso de embalagem pode variar; editar.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Caju", "kg", "caixa", 1.2, "R$/caixa ou R$/kg", null, "Peso de embalagem pode variar; editar.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.VEGETAL, "Fruta", "Abacate", "kg", "caixa", 20.0, "R$/caixa ou R$/kg", null, "Avocado pode usar caixa menor; editar.", "https://ceagesp.gov.br/wp-content/uploads/2015/06/medidaFrutas.pdf"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Leite", "Leite cru", "litro", "litro", 1.0, "R$/litro", TipoProducaoAnimal.CONTINUA, "Produção diária por vaca x dias x perdas.", "https://cepea.org.br/br/indicador/leite.aspx"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Ovos", "Ovos", "ovo", "dúzia", 12.0, "R$/dúzia", TipoProducaoAnimal.CONTINUA, "Produção diária por ave; converter ovos para dúzia.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Aves", "Frango caipira vivo", "kg", "kg", 1.0, "R$/kg vivo", TipoProducaoAnimal.LOTE, "Também pode ser vendido por unidade; se for unidade, use peso médio convertido.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Aves", "Frango de corte", "kg", "kg", 1.0, "R$/kg", TipoProducaoAnimal.LOTE, "Separar vivo/carcaça conforme comprador.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Aves", "Galinha caipira", "unidade", "unidade", 1.0, "R$/unidade", TipoProducaoAnimal.LOTE, "Se vender por kg, alterar unidade para kg.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Suínos", "Suíno vivo", "kg", "kg", 1.0, "R$/kg vivo", TipoProducaoAnimal.LOTE, "CEPEA publica indicador de suíno vivo em R$/kg.", "https://cepea.org.br/br/indicador/suino.aspx"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Suínos", "Leitão", "unidade", "unidade", 1.0, "R$/unidade", TipoProducaoAnimal.LOTE, "Pode ser unidade ou kg vivo; editar conforme venda.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Bovinos", "Bovino de corte", "kg carcaça", "arroba", 15.0, "R$/arroba", TipoProducaoAnimal.LOTE, "Arroba pecuária = 15 kg de carcaça; usar peso de carcaça estimado.", "https://cepea.org.br/br/indicador/boi-gordo.aspx"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Bovinos", "Bezerro", "cabeça", "cabeça", 1.0, "R$/cabeça", TipoProducaoAnimal.LOTE, "Preço por cabeça varia muito por peso/raça/região.", "https://cepea.org.br/"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Ovinos", "Cordeiro/Ovino", "kg", "kg", 1.0, "R$/kg vivo", TipoProducaoAnimal.LOTE, "CEPEA publica cordeiro em R$/kg vivo.", "https://cepea.org.br/br/indicador/cordeiro.aspx"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Caprinos", "Cabrito/Caprino", "kg", "kg", 1.0, "R$/kg vivo", TipoProducaoAnimal.LOTE, "Ajustar conforme comprador local.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Piscicultura", "Tilápia", "kg", "kg", 1.0, "R$/kg", TipoProducaoAnimal.LOTE, "CEPEA publica indicador de tilápia em R$/kg.", "https://cepea.org.br/br/indicador/tilapia.aspx"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Piscicultura", "Peixe redondo/Tambaqui", "kg", "kg", 1.0, "R$/kg", TipoProducaoAnimal.LOTE, "Usar peso final vendável e perdas.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html"),
            new ProdutoPadrao(ModuloProducao.ANIMAL, "Apicultura", "Mel", "kg", "kg", 1.0, "R$/kg", TipoProducaoAnimal.CONTINUA, "Produção média por colmeia no período x perdas.", "https://portaldeinformacoes.conab.gov.br/precos-agropecuarios.html")
    );

    public static final List<ChecklistPadrao> CHECKLIST = List.of(
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Sementes ou mudas", "Entrar pelo valor total do ciclo."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Adubo, calcário, composto ou substrato", "Inclua comprado e produzido na propriedade com custo estimado."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Defensivos, biológicos ou armadilhas", "Inclua tratamento preventivo e corretivo."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Irrigação, água e energia", "Pode entrar por ciclo ou rateio."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Preparo de solo, canteiro ou mecanização", "Terceirizado ou custo próprio rateado."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Mão de obra familiar ou contratada", "Não zerar mão de obra familiar."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Colheita, lavagem, seleção e classificação", "Inclua tempo e material usado."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Embalagem", "Caixa, saco, bandeja, elástico, rótulo."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Frete, comissão e taxas", "Use o valor líquido que realmente sobra."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Perdas esperadas", "Entrar como % de perda."),
            new ChecklistPadrao(ModuloChecklist.VEGETAL, "Rateio de estrutura, ferramentas e aluguel", "Usar rateio simples por ciclo/período."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Ração, milho, farelo, volumoso ou pastagem", "Maior custo em muitos sistemas."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Compra de animais, pintinhos, alevinos ou matrizes", "Entrar no ciclo ou rateio conforme caso."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Medicamentos, vacinas e vermífugos", "Não esquecer manejo sanitário."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Assistência técnica", "Incluir quando pago ou rateado."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Mão de obra", "Rotina diária também tem custo."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Energia, água e combustível", "Ordenha, bomba, refrigeração, transporte."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Cama, limpeza e manejo", "Maravalha, cal, limpeza, manutenção."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Ordenha, coleta, resfriamento ou armazenamento", "Especialmente leite e ovos."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Abate, processamento ou beneficiamento", "Quando vendido abatido/processado."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Embalagem", "Caixa de ovos, pote de mel, saco, rótulo."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Transporte, comissão, taxas e inspeção", "Usar preço líquido final."),
            new ChecklistPadrao(ModuloChecklist.ANIMAL, "Mortalidade, descarte ou quebra", "Entrar como % de perda."),
            new ChecklistPadrao(ModuloChecklist.AMBOS, "Reserva para imprevistos", "Recomendado entre 5% e 15%, editável.")
    );

    private RaioxCatalogoPadrao() {
    }
}
