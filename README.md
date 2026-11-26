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

## Lamplighter
- **HP 100**, dano 20, abre portas, efeito `compound_v_tweaks:lamplighter` permanente. Hostil a jogadores, imune a fogo.
- **Bolas de fogo** (como as do Ghast) e **laser de fogo** (6 de dano por pulso + queima, partículas de chama,
  precisa de linha de visão, acende fogo no ponto final; bem mais fraco que o laser do Adam e não quebra blocos).
- Arma: o **Isqueiro do Lamplighter** (`hellaverse:lighter`), que funciona como isqueiro de pederneira.

## Cherri Bomb
- **HP 18**, dano 2,5, muito rápida, **joga TNT aceso** no alvo e **não sofre dano de explosão**. Hostil a jogadores.

## Alastor (boss)
- **HP 500**, dano 25, **imune a projéteis**, barra de vida verde. Hostil a jogadores.
- **Invoca demônios de sombra** (3 por vez, até 8 vivos) e faz brotar **tentáculos pretos com brilho verde**
  em volta do alvo: aviso verde no chão por 1 s, depois 10 de dano e lentidão forte em quem estiver no local.
  Os tentáculos são desenhados com partículas.
- Segura o **Alastor Staff**, que sempre cai quando ele morre.

## Demônio de Sombra
- **HP 5**, dano 6, rápido. Protege o dono (jogador com o cajado ou o Alastor): ataca quem feriu o dono e o alvo do dono,
  segue o dono, não fere o dono nem outros demônios dele e some após 30 s. Não tem ovo de geração.

## Alastor Staff (`hellaverse:alastor_staff`)
- Botão direito: invoca 3 demônios de sombra que protegem você (máx. 9 ao mesmo tempo, recarga de 20 s, 256 usos).

## Frenchie
- **HP 25**, dano corpo a corpo igual ao de um jogador (1), abre portas, neutro: revida quem o atacar e ataca mobs hostis.
- Atira com as armas do mod (sem gastar munição): **RPG** contra alvos com mais vida máxima que ele, **AK-47 ou revólver**
  (aleatório) contra alvos com vida igual ou menor. A arma aparece na mão dele.
- Skin própria (`textures/entity/frenchie.png`).

## Moxxie
- **HP 15**, dano corpo a corpo 0,5 (metade do jogador), abre portas, imune a fogo, neutro/hostil: revida e ataca mobs hostis.
- Atira de **revólver** (até 24 blocos) ou **sniper** (mais longe). Menor que um jogador (78% do tamanho, hitbox menor).
- Skin própria (`textures/entity/moxxie.png`).

## Asmodeus (boss)
- **HP 2500**, dano 75, **5 blocos de altura**, barra de vida azul, hostil a jogadores, imune a fogo, abre portas.
  Aura de partículas de **fogo azul** (`soul_fire_flame`) em volta do corpo. Skin própria (braço fino); o modelo é
  ampliado automaticamente para 5 blocos.
- **Projétil de fogo azul**: 15 de dano + queima por 6 s, invisível (só as partículas), a cada ~1,5 s a distância.
- **Agarrar**: perto do alvo (até 7 blocos), levanta o alvo à sua frente e despeja **de 4 a 7** projéteis de fogo azul
  nele, depois solta (recarga de 15 s).
- **Agachar**: se não há 5 blocos de altura livres onde está (ou à frente), ou se não acha caminho até o alvo, agacha
  (hitbox 0,9 x 1,9) para passar por portas e vãos; levanta quando cabe de novo.

## Vox (boss)
- **HP 550**, dano 30, hostil a jogadores, abre portas, imune a raios. Skin própria (braço fino).
- **Teletransporte**: reaparece perto do alvo quando está longe (ou de surpresa) e às vezes salta para longe ao ser ferido.
- **Raios**: marca o chão com faíscas por ~0,75 s e então cai um raio no alvo e outro em volta (20 de dano cada).
  Os raios são só visuais: não causam incêndio nem convertem mobs.

## Stormfront (boss)
- **HP 350**, dano 25, hostil a jogadores, **voa**, abre portas (inclusive voando), imune a raios, efeito
  `compound_v:stormfront` permanente. Skin própria em alta resolução (128x128).
- **Raios**: três raios por vez (12 de dano cada), um no alvo e dois em volta, com aviso de faíscas.

## Beelzebub (boss)
- **HP 2100**, dano 90, hostil a jogadores, **voa**, abre portas (inclusive voando). Skin própria em alta resolução (128x128).
- **Mais resistente que o Homelander**: recebe só 50% do dano (o Homelander, 60%) e ainda tem Resistência III
  (o Homelander, II). Imune a empurrão.
- **Fúria** ao chegar em 50% da vida: dano ×1,5 (135), +30% de velocidade, Força II, barra de vida vermelha,
  rugido e partículas de chama.

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
