#!/usr/bin/env bash
set -euo pipefail

API="${API:-http://localhost:8090}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@agro.com}"
ADMIN_SENHA="${ADMIN_SENHA:-admin123}"
DEMO_EMAIL="${DEMO_EMAIL:-demo@agro.com}"
DEMO_SENHA="${DEMO_SENHA:-demo1234}"
PRODUTOR_NOME="Sítio Demonstração"
PRODUTOR_DOCUMENTO="00000000000191"
PAPEL_NOME="Demonstração"

command -v jq >/dev/null || { echo "Precisa do jq instalado." >&2; exit 1; }

chamar() {
  local metodo="$1" caminho="$2" corpo="${3:-}"
  local args=(-sS -X "$metodo" "$API$caminho" -H "Authorization: Bearer $TOKEN" -w '\n%{http_code}')
  [[ -n "$corpo" ]] && args+=(-H 'Content-Type: application/json' -d "$corpo")
  local saida status
  saida=$(curl "${args[@]}")
  status=$(tail -n1 <<<"$saida")
  saida=$(sed '$d' <<<"$saida")
  if (( status >= 300 )); then
    echo "Falhou: $metodo $caminho ($status) $saida" >&2
    exit 1
  fi
  printf '%s' "$saida"
}

TOKEN=$(curl -sS "$API/auth/login" -H 'Content-Type: application/json' \
  -d "$(jq -n --arg e "$ADMIN_EMAIL" --arg s "$ADMIN_SENHA" '{email: $e, senha: $s}')" | jq -r '.token // empty')
[[ -n "$TOKEN" ]] || { echo "Não consegui logar como $ADMIN_EMAIL em $API." >&2; exit 1; }

PRODUTOR_ID=$(chamar GET "/produtores?limit=50&termo=$PRODUTOR_DOCUMENTO" |
  jq -r --arg d "$PRODUTOR_DOCUMENTO" '.conteudo[] | select(.documento == $d) | .id' | head -n1)
if [[ -z "$PRODUTOR_ID" ]]; then
  PRODUTOR_ID=$(chamar POST /produtores "$(jq -n --arg n "$PRODUTOR_NOME" --arg d "$PRODUTOR_DOCUMENTO" \
    '{nome: $n, documento: $d, tipoPessoa: "PJ"}')" | jq -r .id)
  echo "Produtor criado: $PRODUTOR_NOME"
else
  echo "Produtor já existe: $PRODUTOR_NOME"
fi

PAPEL_ID=$(chamar GET "/papeis?limit=100&produtorId=$PRODUTOR_ID" |
  jq -r --arg n "$PAPEL_NOME" '.conteudo[] | select(.nome == $n) | .id' | head -n1)
if [[ -z "$PAPEL_ID" ]]; then
  PAPEL_ID=$(chamar POST /papeis "$(jq -n --arg n "$PAPEL_NOME" --arg p "$PRODUTOR_ID" '{
    nome: $n,
    descricao: "Acesso de demonstração: consulta tudo e usa a calculadora do Raio-X.",
    produtorId: $p,
    permissoes: [
      "USUARIO_LISTAR", "PAPEL_LISTAR",
      "RAIOXPRODUTO_LISTAR", "RAIOXCHECKLIST_LISTAR",
      "RAIOXSIMULACAO_LISTAR", "RAIOXSIMULACAO_INSERIR"
    ]}')" | jq -r .id)
  echo "Papel criado: $PAPEL_NOME"
else
  echo "Papel já existe: $PAPEL_NOME"
fi

USUARIO_ID=$(chamar GET "/usuarios?limit=50&produtorId=$PRODUTOR_ID&termo=$DEMO_EMAIL" |
  jq -r --arg e "$DEMO_EMAIL" '.conteudo[] | select(.email == $e) | .id' | head -n1)
if [[ -z "$USUARIO_ID" ]]; then
  chamar POST /usuarios "$(jq -n --arg e "$DEMO_EMAIL" --arg s "$DEMO_SENHA" --arg pa "$PAPEL_ID" --arg pr "$PRODUTOR_ID" \
    '{nome: "Visitante", email: $e, senha: $s, papelId: $pa, produtorId: $pr}')" >/dev/null
  echo "Usuário criado: $DEMO_EMAIL"
else
  echo "Usuário já existe: $DEMO_EMAIL"
fi

TOTAL=$(chamar GET "/raiox/simulacoes?limit=1&produtorId=$PRODUTOR_ID" | jq -r .total)
if [[ "$TOTAL" != "0" ]]; then
  echo "Simulações de exemplo já existem ($TOTAL)."
  exit 0
fi

salvar() {
  local nome="$1" formulario="$2"
  chamar POST /raiox/simulacoes "$(jq -n --arg n "$nome" --arg p "$PRODUTOR_ID" --argjson f "$formulario" \
    '{nome: $n, produtorId: $p, formulario: $f}')" | jq -r '"Simulação salva: \(.nome) — \(.semaforo), preço mínimo \(.precoMinimo)"'
}

custos() {
  jq -n --argjson v "$1" '{
    insumos: $v[0], aquisicao: $v[1], maoDeObra: $v[2], energia: $v[3], sanidade: $v[4],
    servicos: $v[5], estrutura: $v[6], embalagem: $v[7], frete: $v[8], outros: $v[9],
    variavelPorUnidade: $v[10]}'
}

salvar "Alface na feira — safra de inverno" "$(jq -n --argjson c "$(custos '[950,0,600,120,0,0,200,176,220,50,0]')" '{
  modulo: "VEGETAL", produtoSelecionado: "Alface", produtoPersonalizado: "",
  regiao: "Paraná", canalVenda: "Feira", unidadeTecnica: "cabeça", unidadeComercial: "caixa",
  quantidadePorUnidade: 20, precoEsperado: 70, margemDesejadaPercent: 20, reservaPercent: 10,
  fonteCotacao: "Feira do produtor", dataCotacao: "2026-07-11",
  area: 1000, producaoBrutaInformada: 1000, perdaVegetalPercent: 12,
  custos: $c, custosMarcados: []}')"

salvar "Milho grão — venda para a cooperativa" "$(jq -n --argjson c "$(custos '[98000,0,14000,6000,0,22000,9000,0,8000,4000,0]')" '{
  modulo: "VEGETAL", produtoSelecionado: "Milho grão", produtoPersonalizado: "",
  regiao: "Paraná", canalVenda: "Cooperativa", unidadeTecnica: "kg", unidadeComercial: "saca",
  quantidadePorUnidade: 60, precoEsperado: 62, margemDesejadaPercent: 15, reservaPercent: 5,
  fonteCotacao: "Cooperativa", dataCotacao: "2026-10-20",
  area: 25, produtividade: 7800, perdaVegetalPercent: 4,
  custos: $c, custosMarcados: []}')"

salvar "Tilápia — lote de outubro" "$(jq -n --argjson c "$(custos '[4200,1500,900,350,200,0,300,0,250,100,0]')" '{
  modulo: "ANIMAL", produtoSelecionado: "Tilápia", produtoPersonalizado: "",
  regiao: "Paraná", canalVenda: "Frigorífico", unidadeTecnica: "kg", unidadeComercial: "kg",
  quantidadePorUnidade: 1, precoEsperado: 9, margemDesejadaPercent: 20, reservaPercent: 5,
  fonteCotacao: "Frigorífico da região", dataCotacao: "2026-10-01",
  tipoProducaoAnimal: "LOTE", quantidadeAnimais: 1000, pesoMedioVenda: 0.9,
  perdaMortalidadeAnimalPercent: 10, custos: $c, custosMarcados: []}')"

echo
echo "Pronto. Entre com $DEMO_EMAIL / $DEMO_SENHA"
