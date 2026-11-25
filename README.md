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
| Efeitos | Resistência III e `compound_v:generic` (permanentes) |
| Dano | 35 por golpe (atributo base 24 + espada de netherite 7 + Afiação 7 = 4) |
| Arma | espada de netherite com Afiação 7 |
| Projéteis | imune (flechas quicam); quem atirou vira alvo |
| Explosões | dano reduzido em 90% |
| Movimento | escala paredes |
| Furtividade | outros mobs o enxergam a 25% da distância e levam 5 s para percebê-lo |
| Portas/janelas | quebra portas, vidros e painéis no caminho do alvo (respeita `mobGriefing`) |
| Comportamento | só ataca quem o atacou primeiro e mobs hostis |

## Homelander

Neutro e hostil: ataca **mobs hostis** e revida quem o atacar, inclusive jogadores. Ovo de spawn na aba criativa. Skin própria (128x128).

| Atributo | Valor |
|---|---|
| Vida / dano | 1000 / 150 |
| Efeitos | Resistência II, `compound_v:laser_eyes_advanced`, `compound_v:creative_flight` (permanentes) |
| Projéteis | imune (balas das armas contam como projéteis) |
| Dano recebido | −40% em tudo (além da Resistência II), exceto `/kill` e vazio |
| Voo | sempre voando (hover) |
| Laser | feixe vermelho dos olhos (20 de dano a cada 5 ticks por 1,5 s, alcance 4 a 48 blocos), com linha de visão |
| Detecção | enxerga mobs hostis a até 64 blocos, mesmo atrás de paredes |
| Blocos/portas | abre portas e quebra blocos no caminho, **somente** enquanto persegue um mob (respeita `mobGriefing`) |

## Kimiko

Neutra e passiva: só revida quem a atacar. Skin própria.

| Atributo | Valor |
|---|---|
| Vida / dano | 55 / 20 |
| Efeitos | Regeneração III (permanente) |
| Portas | abre portas e quebra as que não consegue abrir |

As skins ficam em `textures/entity/<nome>.png` (modelo de jogador, braços clássicos). Para trocar uma, basta substituir o PNG.

## Web Weaver

Neutro e passivo: só revida quem o atacar. Skin própria.

| Atributo | Valor |
|---|---|
| Vida / dano | 30 / 6,5 |
| Movimento | escala paredes |
| Portas | abre portas |
| Teias | atira uma teia no alvo (3 a 14 blocos): Lentidão III por 3 s e uma teia no local, que some em 10 s |

## Adam (boss)
- **HP 650**, dano 60, voa, barra de vida de boss (amarela). Skin própria (braço fino).
- **Laser**: 30 de dano por pulso, atravessa paredes e **destrói qualquer bloco quebrável** no caminho
  (respeita a gamerule `mobGriefing`; blocos inquebráveis como bedrock e barreira não são afetados).
- Hostil: ataca jogadores e quem o atacar. Ovo de geração: `Adam`.

## Soldier Boy (boss)
- **HP 450**, dano 75 por golpe (46 base + 29 do escudo), **imune a projéteis**, Resistência II e -25% de dano
  (menos resistente que o Homelander, que tem -40%). Barra de vida azul.
- **Chest blast** (`compound_v:chest_blast`): o efeito é concedido, e como o Compound V só o executa em jogadores,
  o mob usa uma explosão de peito própria (carrega 2 s e detona perto do alvo).
- **Escudo do Soldier Boy** (`hellaverse:soldier_boy_shield`): sempre dropa ao morrer, 30 de dano corpo a corpo,
  bloqueia como um escudo normal e tem 3000 de durabilidade (netherite = 2031).
- O escudo aparece como sprite 2D (sem modelo 3D de escudo).
- Hostil: ataca jogadores e quem o atacar.

## Armas e munições

Na aba criativa (e por receita de craft). Cada tiro gasta 1 munição do tipo da arma, que fica no inventário (ou na mão secundária). No criativo não gasta. Armas automáticas atiram enquanto o botão direito estiver pressionado.

| Arma | Munição | Dano | Cadência | Alcance | Tipo |
|---|---|---|---|---|---|
| Revólver | Munição de Pistola | 12 | 0,7 s | 64 | um tiro por clique |
| Uzi | Munição de Submetralhadora | 3,5 | 0,1 s (dispersão 3°) | 40 | automática |
| AK-47 | Munição de Fuzil | 6 | 0,15 s (dispersão 1,5°) | 64 | automática |
| Sniper | Munição de Sniper | 40 | 2 s | 160 | um tiro por clique |
| RPG | Foguete de RPG | explosão (força 3,5) | 3 s | foguete | um tiro por clique |

As balas são de acerto instantâneo e contam como projéteis: Black Noir e Homelander são imunes a elas. O foguete do RPG explode, então Black Noir só leva 10% do dano. Os números ficam em `ModItems.java` (`GunItem.Stats`).

Receitas de munição: Pistola = lingote de cobre + pólvora (8); Submetralhadora = 3 pepitas de ferro + pólvora (12); Fuzil = lingote de ferro + pólvora (10); Sniper = lingote de ferro + 2 pólvoras (5); Foguete = TNT + lingote de ferro + pólvora (2). As armas usam lingotes de ferro, redstone, gravetos, bloco de ferro (AK-47, Sniper, RPG) e TNT (RPG).

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
- `entity/`, `event/`: Black Noir, Homelander, Kimiko, os goals de IA (laser, quebra de blocos) e a furtividade.
- `client/`: céu do Orgulho (pentagrama) e renderers dos personagens.
- `tools/`: gerador de dados e texturas.
