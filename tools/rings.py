"""Definicao de cada anel/dimensao. Cores no formato 0xRRGGBB.

Para criar uma nova dimensao: adicione uma entrada aqui, adicione o enum em
src/main/java/com/hellaverse/Ring.java e rode `python3 tools/generate_assets.py`.
"""

MONSTERS = [
    {"type": "minecraft:zombie", "weight": 100, "minCount": 4, "maxCount": 4},
    {"type": "minecraft:skeleton", "weight": 100, "minCount": 4, "maxCount": 4},
    {"type": "minecraft:spider", "weight": 100, "minCount": 4, "maxCount": 4},
    {"type": "minecraft:creeper", "weight": 50, "minCount": 4, "maxCount": 4},
]


def extra(entity, weight, lo=1, hi=2):
    return {"type": f"minecraft:{entity}", "weight": weight, "minCount": lo, "maxCount": hi}


RINGS = [
    dict(
        id="orgulho", pt="Anel do Orgulho", en="Ring of Pride",
        soil_pt="Terra Negra do Orgulho", soil_en="Pride Black Soil",
        soil=0x17141A, fleck=0x5A0A12, sky=0xB00000, fog=0x6E0000,
        water=0xC8001E, water_fog=0x4A0008, key=0xC8001E,
        fixed_time=6000, effects="hellaverse:orgulho", dark=True,
        monsters=MONSTERS + [extra("wither_skeleton", 30)], features=[],
    ),
    dict(
        id="ira", pt="Anel da Ira", en="Ring of Wrath",
        soil_pt="Terra Rubra da Ira", soil_en="Wrath Red Soil",
        soil=0x9A2A1A, fleck=0xE0561E, sky=0xC8400F, fog=0x8A2A0A,
        water=0x3F76E4, water_fog=0x050533, key=0xE0561E,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS + [extra("blaze", 20)],
        features=["hellaverse:volcano", "hellaverse:farm"],
    ),
    dict(
        id="ganancia", pt="Anel da Ganância", en="Ring of Greed",
        soil_pt="Terra Negra da Ganância", soil_en="Greed Black Soil",
        soil=0x14171A, fleck=0x1FA89A, sky=0x1FA8A0, fog=0x14736E,
        water=0x1E8F85, water_fog=0x07302D, key=0x1FA89A,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS + [extra("piglin", 40, 1, 3)], features=[],
    ),
    dict(
        id="luxuria", pt="Anel da Luxúria", en="Ring of Lust",
        soil_pt="Terra Púrpura da Luxúria", soil_en="Lust Purple Soil",
        soil=0x7A3C96, fleck=0xE46AB8, sky=0xB44CC8, fog=0x8A3A9E,
        water=0x9B3FB5, water_fog=0x3A0F4A, key=0xE46AB8,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS + [extra("witch", 25, 1, 1)], features=[],
    ),
    dict(
        id="gula", pt="Anel da Gula", en="Ring of Gluttony",
        soil_pt="Terra Amarela da Gula", soil_en="Gluttony Yellow Soil",
        soil=0xE3C93A, fleck=0xB8860B, sky=0xF0D820, fog=0xC8B020,
        water=0xC9A92B, water_fog=0x4D4010, key=0xE3C93A,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS + [extra("hoglin", 20, 1, 3)], features=[],
    ),
    dict(
        id="inveja", pt="Anel da Inveja", en="Ring of Envy",
        soil_pt="Terra Roxa da Inveja", soil_en="Envy Purple Soil",
        soil=0x5E2A7E, fleck=0x4C8F2A, sky=0x6B7F3F, fog=0x3E4A2A,
        water=0x6A2C91, water_fog=0x2E0F45, key=0x4C8F2A,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS + [extra("slime", 40, 1, 2), extra("drowned", 30, 1, 2)], features=[],
    ),
    dict(
        id="preguica", pt="Anel da Preguiça", en="Ring of Sloth",
        soil_pt="Terra Rosa da Preguiça", soil_en="Sloth Pink Soil",
        soil=0xF2A6C8, fleck=0xFFD6E8, sky=0xFFB6D9, fog=0xF4A0C8,
        water=0xE88AC0, water_fog=0x5A2A48, key=0xF2A6C8,
        fixed_time=None, effects="minecraft:overworld", dark=True,
        monsters=MONSTERS, features=[],
    ),
    dict(
        id="paraiso", pt="Paraíso", en="Paradise",
        soil_pt="Terra Branca do Paraíso", soil_en="Paradise White Soil",
        soil=0xF2F2F2, fleck=0xBFD8FF, sky=0x78A7FF, fog=0xC0D8FF,
        water=0x3F76E4, water_fog=0x050533, key=0xFFFFFF,
        fixed_time=None, effects="minecraft:overworld", dark=False,
        monsters=[], features=[],
    ),
]
