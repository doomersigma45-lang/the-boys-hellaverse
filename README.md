# The Boys Hellaverse

Mod de Minecraft **1.20.1 (Forge 47.3.0)** com oito dimensões temáticas baseadas nos pecados capitais mais o Paraíso.

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

## Como viajar (provisório)

Na aba criativa **The Boys Hellaverse** há uma **Chave** por anel. Botão direito com a chave leva para o anel (mesmas coordenadas X/Z). Usar a chave dentro do anel volta ao Overworld. Portais de verdade ficam para uma próxima etapa.

Também funciona por comando: `/execute in hellaverse:orgulho run tp @s ~ 100 ~`

## Compilar

Precisa de Java 17.

```
./gradlew build        # jar em build/libs/
./gradlew runClient    # abre o Minecraft para testar
```

Cada push também compila no GitHub Actions; o jar fica em *Actions → Build → Artifacts*.

## Adicionar uma nova dimensão

1. Adicione uma entrada em `tools/rings.py` (cores, mobs, features).
2. Adicione o enum correspondente em `src/main/java/com/hellaverse/Ring.java`.
3. Rode `python3 tools/generate_assets.py` (precisa de Pillow). Ele gera dimensão, bioma, bloco, chave, texturas e traduções.

## Estrutura

- `registry/`: blocos, itens, aba criativa, features e gerador de chunks.
- `worldgen/`: `RingChunkGenerator`, `VolcanoFeature`, `FarmFeature`.
- `client/`: céu do Orgulho (pentagrama).
- `tools/`: gerador de dados e texturas.
