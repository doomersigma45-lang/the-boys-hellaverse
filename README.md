# The Boys Hellaverse

Mod de Minecraft **1.20.1 (Forge 47.3.0)** com oito dimensões temáticas baseadas nos pecados capitais mais o Paraíso.

## Dependências (obrigatórias)

O mod só abre com estes mods instalados:

- **Compound V** 2.3.6 ou superior
- **Compound V Tweaks** 5.2.0 ou superior (que por sua vez exige o **GeckoLib** 4.4.9+)

## Dimensões

| # | ID da dimensão | Nome | Terra | Céu | Extras |
|---|---|---|---|---|---|
| 1 | `hellaverse:orgulho` | Anel do Orgulho | preta | vermelho com pentagrama giratório (sem sol/lua) | água vermelha, dia fixo |
| 2 | `hellaverse:ira` | Anel da Ira | vermelha | laranja-avermelhado | vulcões com cratera de lava, fazendas |
| 3 | `hellaverse:ganancia` | Anel da Ganância | preta | verde-azulado | — |
| 4 | `hellaverse:luxuria` | Anel da Luxúria | roxa | roxo-rosado | — |
| 5 | `hellaverse:gula` | Anel da Gula | amarela | amarelo | — |
| 6 | `hellaverse:inveja` | Anel da Inveja | roxa | verde-oliva | água roxa (oceano/pântano) |
| 7 | `hellaverse:preguica` | Preguiça | rosa | rosa | — |
| 8 | `hellaverse:paraiso` | Paraíso | branca | azul, igual ao Overworld | só animais pacíficos |

O relevo é o mesmo do Overworld (montanhas, rios, cavernas, minérios). Um gerador de chunks próprio (`RingChunkGenerator`) troca pedra, terra, grama e areia pelo bloco de terra do anel. A cor da água e do céu vem do bioma de cada dimensão.

## Black Noir

Mob neutro com a skin enviada, disponível no ovo de spawn da aba criativa.

| Atributo | Valor |
|---|---|
| Vida | 130 |
| Dano | 35 por golpe (atributo base 24 + espada de netherite 7 + Afiação 7 = 4) |
| Arma | espada de netherite com Afiação 7 |
| Projéteis | imune (flechas quicam); quem atirou vira alvo |
| Explosões | dano reduzido em 90% |
| Movimento | escala paredes |
| Furtividade | outros mobs o enxergam a 25% da distância e levam 5 s para percebê-lo |
| Portas/janelas | quebra portas, vidros e painéis no caminho do alvo (respeita `mobGriefing`) |
| Comportamento | só ataca quem o atacou primeiro e mobs hostis |

## Como viajar (provisório)

Na aba criativa **The Boys Hellaverse** há uma **Chave** por anel. Botão direito com a chave leva para o anel (mesmas coordenadas X/Z). Usar a chave dentro do anel volta ao Overworld. Portais de verdade ficam para uma próxima etapa.

Também funciona por comando: `/execute in hellaverse:orgulho run tp @s ~ 100 ~`

## Compilar

Precisa de Java 17.

```
./gradlew build        # jar em build/libs/
./gradlew runClient    # abre o Minecraft para testar
```

Para testar com `runClient`, coloque os jars do Compound V, Compound V Tweaks e GeckoLib em `run/mods/`.

Cada push também compila no GitHub Actions; o jar fica em *Actions → Build → Artifacts*.

## Adicionar uma nova dimensão

1. Adicione uma entrada em `tools/rings.py` (cores, mobs, features).
2. Adicione o enum correspondente em `src/main/java/com/hellaverse/Ring.java`.
3. Rode `python3 tools/generate_assets.py` (precisa de Pillow). Ele gera dimensão, bioma, bloco, chave, texturas e traduções.

## Estrutura

- `registry/`: blocos, itens, aba criativa, features e gerador de chunks.
- `worldgen/`: `RingChunkGenerator`, `VolcanoFeature`, `FarmFeature`.
- `entity/`, `event/`: Black Noir, seus goals de IA e a furtividade.
- `client/`: céu do Orgulho (pentagrama) e renderer do Black Noir.
- `tools/`: gerador de dados e texturas.
