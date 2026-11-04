package br.com.codenest.agro.raiox.simulacao.calculo;

import br.com.codenest.agro.raiox.produto.enums.ModuloProducao;
import br.com.codenest.agro.raiox.produto.enums.TipoProducaoAnimal;
import br.com.codenest.agro.raiox.simulacao.enums.Semaforo;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Calcula o preço mínimo de venda de um produto a partir da produção e dos
 * custos do ciclo, e classifica o preço esperado em um semáforo.
 *
 * <p>Não acessa banco nem estado: o mesmo formulário sempre gera o mesmo
 * resultado. Faltando dado obrigatório, o cálculo não falha — devolve os
 * valores que deu para chegar, a lista de erros e o semáforo em
 * {@link Semaforo#FILL}.
 */
@ApplicationScoped
public class CalculadoraPrecoMinimo {

    private static final MathContext PRECISAO = MathContext.DECIMAL64;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal RESERVA_MAXIMA = BigDecimal.valueOf(50);
    private static final BigDecimal PERDA_VEGETAL_MAXIMA = BigDecimal.valueOf(80);
    private static final Set<String> ROTULOS_DE_UNIDADE = Set.of("unidade", "unidades", "cabeca", "cabecas");

    public ResultadoCalculo calcular(FormularioCalculo formulario) {
        List<String> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();

        BigDecimal quantidadePorUnidade = positivo(formulario.quantidadePorUnidade());
        BigDecimal precoEsperado = positivo(formulario.precoEsperado());
        BigDecimal margemDesejadaPercent = numeroOuZero(formulario.margemDesejadaPercent());
        BigDecimal reservaPercent = numeroOuZero(formulario.reservaPercent());

        if (texto(formulario.unidadeComercial()).isEmpty()) {
            erros.add("Informe a unidade comercial de venda.");
        }
        if (quantidadePorUnidade == null) {
            erros.add("A quantidade por unidade comercial deve ser maior que zero.");
        }
        if (precoEsperado == null) {
            erros.add("O preço líquido esperado deve ser maior que zero.");
        }
        if (negativo(margemDesejadaPercent)) {
            erros.add("A margem desejada não pode ser negativa.");
        }
        boolean reservaValida = !negativo(reservaPercent) && reservaPercent.compareTo(RESERVA_MAXIMA) <= 0;
        if (!reservaValida) {
            erros.add("A reserva para imprevistos deve ficar entre 0% e 50%.");
        }

        if (formulario.nomeProduto().isEmpty()) {
            avisos.add("Informe o produto ou atividade para identificar o cálculo.");
        }
        if (texto(formulario.fonteCotacao()).isEmpty()) {
            avisos.add("Preço sem fonte: valide a cotação antes de decidir.");
        }

        BigDecimal producaoTecnicaBruta = null;
        BigDecimal producaoTecnicaVendavel = null;

        if (formulario.modulo() == ModuloProducao.VEGETAL) {
            BigDecimal brutaInformada = positivo(formulario.producaoBrutaInformada());
            BigDecimal area = positivo(formulario.area());
            BigDecimal produtividade = positivo(formulario.produtividade());
            BigDecimal percentPerda = numeroOuZero(formulario.perdaVegetalPercent());
            boolean perdaValida = !negativo(percentPerda) && percentPerda.compareTo(PERDA_VEGETAL_MAXIMA) <= 0;

            if (!perdaValida) {
                erros.add("A perda vegetal deve ficar entre 0% e 80%.");
            }

            if (brutaInformada != null) {
                producaoTecnicaBruta = brutaInformada;
            } else if (area != null && produtividade != null) {
                producaoTecnicaBruta = area.multiply(produtividade, PRECISAO);
            }

            if (producaoTecnicaBruta == null) {
                erros.add("Informe a produção bruta ou preencha área e produtividade com valores maiores que zero.");
            } else if (perdaValida) {
                producaoTecnicaVendavel = descontarPerda(producaoTecnicaBruta, percentPerda);
            }

            if (formulario.perdaVegetalPercent() == null) {
                avisos.add("Perda vegetal vazia: considerando 0%.");
            }
        } else {
            BigDecimal quantidadeAnimais = positivo(formulario.quantidadeAnimais());
            BigDecimal percentPerda = numeroOuZero(formulario.perdaMortalidadeAnimalPercent());
            boolean perdaValida = !negativo(percentPerda) && percentPerda.compareTo(CEM) < 0;

            if (quantidadeAnimais == null) {
                erros.add("Informe o número de animais, matrizes ou colmeias.");
            }
            if (!perdaValida) {
                erros.add("A perda ou mortalidade animal deve ficar entre 0% e menos de 100%.");
            }

            if (formulario.tipoProducaoAnimal() == TipoProducaoAnimal.CONTINUA) {
                BigDecimal producaoDiaria = positivo(formulario.producaoDiariaMedia());
                BigDecimal dias = positivo(formulario.diasPeriodo());
                if (producaoDiaria == null) {
                    erros.add("Informe a produção média por animal ou colmeia no período diário.");
                }
                if (dias == null) {
                    erros.add("Informe a quantidade de dias do período.");
                }
                if (quantidadeAnimais != null && producaoDiaria != null && dias != null) {
                    producaoTecnicaBruta = quantidadeAnimais.multiply(producaoDiaria).multiply(dias, PRECISAO);
                }
            } else if (ehLoteAnimalPorUnidade(
                    formulario.unidadeTecnica(), formulario.unidadeComercial(), quantidadePorUnidade)) {
                producaoTecnicaBruta = quantidadeAnimais;
            } else {
                BigDecimal pesoMedio = positivo(formulario.pesoMedioVenda());
                if (pesoMedio == null) {
                    erros.add("Informe o peso médio de venda por animal.");
                }
                if (quantidadeAnimais != null && pesoMedio != null) {
                    producaoTecnicaBruta = quantidadeAnimais.multiply(pesoMedio, PRECISAO);
                }
            }

            if (producaoTecnicaBruta != null && perdaValida) {
                producaoTecnicaVendavel = descontarPerda(producaoTecnicaBruta, percentPerda);
            }

            if (formulario.perdaMortalidadeAnimalPercent() == null) {
                avisos.add("Perda animal vazia: considerando 0%.");
            }
        }

        BigDecimal producaoComercial = producaoTecnicaVendavel != null
                        && producaoTecnicaVendavel.signum() != 0
                        && quantidadePorUnidade != null
                ? producaoTecnicaVendavel.divide(quantidadePorUnidade, PRECISAO)
                : null;

        Custos custos = formulario.custos();
        boolean custosInvalidos = custos.todos().stream().anyMatch(valor -> negativo(numeroOuZero(valor)));
        if (custosInvalidos) {
            erros.add("Os custos não podem conter valores negativos.");
        }

        BigDecimal custosFixos = custos.fixos().stream()
                .map(valor -> numeroOuZero(valor).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variavelPorUnidade = numeroOuZero(custos.variavelPorUnidade()).max(BigDecimal.ZERO);
        BigDecimal custoBase = producaoComercial != null && !custosInvalidos
                ? custosFixos.add(variavelPorUnidade.multiply(producaoComercial, PRECISAO))
                : null;

        if (custosFixos.signum() <= 0 && variavelPorUnidade.signum() <= 0) {
            erros.add("Informe ao menos um custo do ciclo ou período.");
        }

        BigDecimal valorReserva = custoBase != null && reservaValida
                ? custoBase.multiply(fracao(reservaPercent), PRECISAO)
                : null;
        BigDecimal custoTotal = custoBase != null && valorReserva != null ? custoBase.add(valorReserva) : null;
        BigDecimal precoMinimo = custoTotal != null && producaoComercial != null && producaoComercial.signum() > 0
                ? custoTotal.divide(producaoComercial, PRECISAO)
                : null;
        BigDecimal precoAlvo = precoMinimo != null && !negativo(margemDesejadaPercent)
                ? precoMinimo.multiply(BigDecimal.ONE.add(fracao(margemDesejadaPercent)), PRECISAO)
                : null;
        BigDecimal receitaEsperada = producaoComercial != null && precoEsperado != null
                ? producaoComercial.multiply(precoEsperado, PRECISAO)
                : null;
        BigDecimal resultadoEstimado =
                receitaEsperada != null && custoTotal != null ? receitaEsperada.subtract(custoTotal) : null;
        BigDecimal margemSegurancaReal = precoEsperado != null && precoMinimo != null && precoMinimo.signum() > 0
                ? precoEsperado.divide(precoMinimo, PRECISAO).subtract(BigDecimal.ONE)
                : null;

        Semaforo semaforo = Semaforo.FILL;
        String mensagem = "Complete os dados obrigatórios antes de decidir.";
        String acao = "Revise unidade, produção, custos e preço.";

        if (erros.isEmpty() && precoEsperado != null && precoMinimo != null && precoAlvo != null) {
            if (precoEsperado.compareTo(precoMinimo) < 0) {
                semaforo = Semaforo.RED;
                mensagem = "Venda ou produção com risco de prejuízo.";
                acao = "Renegocie, reduza custos, mude o canal ou revise a produção.";
            } else if (precoEsperado.compareTo(precoAlvo) < 0) {
                semaforo = Semaforo.YELLOW;
                mensagem = "Cobre o custo, mas a folga é baixa.";
                acao = "Busque preço melhor, reduza perdas ou corte custos.";
            } else {
                semaforo = Semaforo.GREEN;
                mensagem = "Preço acima do mínimo com margem de segurança.";
                acao = "Avance com controle e mantenha os dados atualizados.";
            }
        }

        return new ResultadoCalculo(
                limpo(producaoTecnicaBruta),
                limpo(producaoTecnicaVendavel),
                limpo(producaoComercial),
                limpo(custoBase),
                limpo(valorReserva),
                limpo(custoTotal),
                limpo(precoMinimo),
                limpo(precoAlvo),
                limpo(receitaEsperada),
                limpo(resultadoEstimado),
                limpo(margemSegurancaReal),
                semaforo,
                mensagem,
                acao,
                List.copyOf(new LinkedHashSet<>(erros)),
                List.copyOf(new LinkedHashSet<>(avisos)));
    }

    /** Lote animal vendido por cabeça ou unidade não precisa de peso médio. */
    static boolean ehLoteAnimalPorUnidade(
            String unidadeTecnica, String unidadeComercial, BigDecimal quantidadePorUnidade) {
        return quantidadePorUnidade != null
                && quantidadePorUnidade.compareTo(BigDecimal.ONE) == 0
                && ROTULOS_DE_UNIDADE.contains(unidadeNormalizada(unidadeTecnica))
                && ROTULOS_DE_UNIDADE.contains(unidadeNormalizada(unidadeComercial));
    }

    private static String unidadeNormalizada(String valor) {
        return Normalizer.normalize(texto(valor), Normalizer.Form.NFD)
                .replaceAll("[\\u0300-\\u036f]", "")
                .toLowerCase(Locale.ROOT);
    }

    private static BigDecimal descontarPerda(BigDecimal producao, BigDecimal percentPerda) {
        return producao.multiply(BigDecimal.ONE.subtract(fracao(percentPerda)), PRECISAO);
    }

    private static BigDecimal numeroOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private static BigDecimal positivo(BigDecimal valor) {
        return valor != null && valor.signum() > 0 ? valor : null;
    }

    private static boolean negativo(BigDecimal valor) {
        return valor.signum() < 0;
    }

    private static BigDecimal fracao(BigDecimal percentual) {
        return percentual.divide(CEM, PRECISAO);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.strip();
    }

    private static BigDecimal limpo(BigDecimal valor) {
        return valor == null ? null : new BigDecimal(valor.stripTrailingZeros().toPlainString());
    }
}
