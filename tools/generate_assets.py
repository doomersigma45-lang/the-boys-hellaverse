#!/usr/bin/env python3
"""Gera todos os JSONs de dados/recursos e as texturas das dimensoes do mod.

Uso (na raiz do repositorio):  python3 tools/generate_assets.py
Requer Pillow (pip install pillow). Edite tools/rings.py para mudar/adicionar dimensoes.
"""
import json
import math
import os
import random
import sys

from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
from rings import RINGS  # noqa: E402

MOD = "hellaverse"
ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", MOD)
DATA = os.path.join(ROOT, "data", MOD)
VANILLA_DATA = os.path.join(ROOT, "data", "minecraft")

# GenerationStep: 4 = SURFACE_STRUCTURES, 6 = UNDERGROUND_ORES, 9 = VEGETAL_DECORATION
FEATURE_STEPS = {"hellaverse:volcano": 4, "hellaverse:farm": 9}
ORES = [
    "minecraft:ore_coal_upper", "minecraft:ore_coal_lower", "minecraft:ore_iron_upper",
    "minecraft:ore_iron_middle", "minecraft:ore_gold", "minecraft:ore_redstone",
    "minecraft:ore_diamond", "minecraft:ore_lapis", "minecraft:ore_copper",
]


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def rgb(value):
    return (value >> 16) & 255, (value >> 8) & 255, value & 255


def save_png(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


# ----------------------------------------------------------------------------- texturas
def soil_texture(ring):
    rnd = random.Random(ring["id"])
    base, fleck = rgb(ring["soil"]), rgb(ring["fleck"])
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            k = 1.0 + rnd.uniform(-0.12, 0.12)
            color = [min(255, int(c * k)) for c in base]
            if rnd.random() < 0.09:
                color = [int(c * 0.35 + f * 0.65) for c, f in zip(color, fleck)]
            img.putpixel((x, y), (*color, 255))
    return img


def key_texture(ring):
    color = rgb(ring["key"])
    dark = tuple(int(c * 0.35) for c in color)
    light = tuple(min(255, int(c * 0.5 + 128)) for c in color)
    body = set()
    for dx in range(-3, 4):  # argola (bow)
        for dy in range(-3, 4):
            if 5 <= dx * dx + dy * dy <= 10:
                body.add((5 + dx, 5 + dy))
    for i in range(7, 14):   # haste
        body.add((i, i))
    body.update({(10, 12), (11, 13), (12, 10), (13, 11)})  # dentes
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for (x, y) in body:
        for ox in (-1, 0, 1):
            for oy in (-1, 0, 1):
                nx, ny = x + ox, y + oy
                if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in body:
                    img.putpixel((nx, ny), (*dark, 255))
    for (x, y) in body:
        if 0 <= x < 16 and 0 <= y < 16:
            img.putpixel((x, y), (*color, 255))
    img.putpixel((3, 3), (*light, 255))
    img.putpixel((8, 8), (*light, 255))
    return img


def pentagram_texture(size=512):
    scale = 4
    big = size * scale
    core, glow = (255, 45, 35, 255), (200, 0, 0, 255)
    cx = cy = big / 2
    radius = big * 0.40
    width = max(10, big // 75)

    def shapes(draw, w, color):
        for r, rw in ((radius * 1.12, w), (radius, w * 2 // 3)):
            draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=color, width=int(rw))
        pts = [(cx + radius * math.cos(math.radians(-90 + 72 * k)),
                cy + radius * math.sin(math.radians(-90 + 72 * k))) for k in range(5)]
        for k in range(5):
            draw.line([pts[k], pts[(k + 2) % 5]], fill=color, width=int(w))

    glow_layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    shapes(ImageDraw.Draw(glow_layer), width * 3, glow)
    glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(big * 0.012))
    line_layer = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    shapes(ImageDraw.Draw(line_layer), width, core)
    out = Image.alpha_composite(glow_layer, line_layer)
    return out.resize((size, size), Image.LANCZOS)


# ----------------------------------------------------------------------------- recursos
def gen_block_and_item_assets(ring):
    rid = ring["id"]
    soil, key = f"{rid}_soil", f"{rid}_key"
    save_png(soil_texture(ring), f"{ASSETS}/textures/block/{soil}.png")
    save_png(key_texture(ring), f"{ASSETS}/textures/item/{key}.png")
    write_json(f"{ASSETS}/blockstates/{soil}.json", {"variants": {"": {"model": f"{MOD}:block/{soil}"}}})
    write_json(f"{ASSETS}/models/block/{soil}.json",
               {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{soil}"}})
    write_json(f"{ASSETS}/models/item/{soil}.json", {"parent": f"{MOD}:block/{soil}"})
    write_json(f"{ASSETS}/models/item/{key}.json",
               {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{key}"}})
    write_json(f"{DATA}/loot_tables/blocks/{soil}.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "entries": [{"type": "minecraft:item", "name": f"{MOD}:{soil}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    })


# ----------------------------------------------------------------------------- armas e municao
STEEL, DARK, BLACK = (150, 155, 165), (70, 72, 82), (35, 35, 40)
WOOD, OLIVE, RED = (125, 80, 42), (92, 104, 62), (190, 60, 42)
BRASS, COPPER = (214, 170, 70), (184, 100, 60)

# (x0, y0, x1, y1, cor) inclusivo; desenhados em ordem.
GUN_SHAPES = {
    "revolver": [(7, 4, 14, 5, STEEL), (4, 4, 8, 8, DARK), (5, 5, 7, 7, STEEL), (2, 8, 4, 12, WOOD),
                 (4, 7, 6, 8, DARK), (5, 8, 6, 9, DARK)],
    "uzi": [(3, 4, 12, 8, DARK), (12, 5, 15, 6, STEEL), (4, 3, 10, 3, STEEL), (6, 9, 8, 14, BLACK),
            (3, 9, 5, 12, BLACK)],
    "ak47": [(9, 5, 15, 5, STEEL), (4, 5, 10, 7, DARK), (0, 5, 4, 8, WOOD), (9, 6, 13, 7, WOOD),
             (7, 8, 9, 11, BLACK), (8, 11, 10, 13, BLACK), (4, 8, 6, 11, WOOD)],
    "sniper": [(6, 6, 15, 6, STEEL), (3, 5, 8, 8, DARK), (0, 6, 3, 9, WOOD), (5, 3, 10, 4, BLACK),
               (6, 4, 7, 5, BLACK), (11, 7, 12, 9, DARK), (4, 9, 5, 11, WOOD)],
    "rpg": [(2, 6, 12, 8, OLIVE), (12, 5, 14, 9, RED), (14, 6, 15, 8, RED), (5, 9, 6, 11, DARK),
            (0, 5, 2, 9, DARK)],
}
AMMO_SHAPES = {
    "pistol_ammo": [(3, 6, 4, 11, BRASS), (3, 5, 4, 5, COPPER), (7, 6, 8, 11, BRASS), (7, 5, 8, 5, COPPER),
                    (11, 6, 12, 11, BRASS), (11, 5, 12, 5, COPPER)],
    "smg_ammo": [(2, 8, 3, 12, BRASS), (2, 7, 3, 7, COPPER), (5, 8, 6, 12, BRASS), (5, 7, 6, 7, COPPER),
                 (8, 8, 9, 12, BRASS), (8, 7, 9, 7, COPPER), (11, 8, 12, 12, BRASS), (11, 7, 12, 7, COPPER)],
    "rifle_ammo": [(4, 4, 6, 12, BRASS), (4, 2, 6, 3, COPPER), (9, 4, 11, 12, BRASS), (9, 2, 11, 3, COPPER)],
    "sniper_ammo": [(6, 5, 9, 13, BRASS), (6, 2, 9, 4, COPPER), (7, 1, 8, 1, COPPER)],
    "rpg_rocket": [(6, 5, 9, 12, OLIVE), (7, 2, 8, 4, RED), (5, 11, 10, 13, DARK), (6, 13, 9, 14, BRASS)],
}


def pixel_art(shapes):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x0, y0, x1, y1, color in shapes:
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                img.putpixel((x, y), (*color, 255))
    out = img.copy()
    for x in range(16):
        for y in range(16):
            if img.getpixel((x, y))[3] == 0:
                near = any(0 <= x + dx < 16 and 0 <= y + dy < 16 and img.getpixel((x + dx, y + dy))[3] > 0
                           for dx in (-1, 0, 1) for dy in (-1, 0, 1))
                if near:
                    out.putpixel((x, y), (20, 20, 24, 255))
    return out


WEAPON_NAMES = {  # id: (en, pt)
    "revolver": ("Revolver", "Revólver"), "uzi": ("Uzi", "Uzi"), "ak47": ("AK-47", "AK-47"),
    "sniper": ("Sniper Rifle", "Rifle Sniper"), "rpg": ("RPG", "RPG"),
    "pistol_ammo": ("Pistol Ammo", "Munição de Pistola"), "smg_ammo": ("SMG Ammo", "Munição de Submetralhadora"),
    "rifle_ammo": ("Rifle Ammo", "Munição de Fuzil"), "sniper_ammo": ("Sniper Ammo", "Munição de Sniper"),
    "rpg_rocket": ("RPG Rocket", "Foguete de RPG"),
    "soldier_boy_shield": ("Soldier Boy's Shield", "Escudo do Soldier Boy"),
}


def _ing(item):
    return {"item": item}


def gen_weapons():
    for name, shapes in {**GUN_SHAPES, **AMMO_SHAPES}.items():
        save_png(pixel_art(shapes), f"{ASSETS}/textures/item/{name}.png")
        write_json(f"{ASSETS}/models/item/{name}.json",
                   {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{name}"}})

    def shaped(name, pattern, key, count=1):
        write_json(f"{DATA}/recipes/{name}.json", {
            "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
            "key": {k: _ing(v) for k, v in key.items()}, "result": {"item": f"{MOD}:{name}", "count": count}})

    def shapeless(name, items, count):
        write_json(f"{DATA}/recipes/{name}.json", {
            "type": "minecraft:crafting_shapeless", "category": "equipment",
            "ingredients": [_ing(i) for i in items], "result": {"item": f"{MOD}:{name}", "count": count}})

    iron, stick, red = "minecraft:iron_ingot", "minecraft:stick", "minecraft:redstone"
    block, tnt, powder = "minecraft:iron_block", "minecraft:tnt", "minecraft:gunpowder"
    shaped("revolver", ["II ", " RS", " S "], {"I": iron, "R": red, "S": stick})
    shaped("uzi", ["III", "IRS", " S "], {"I": iron, "R": red, "S": stick})
    shaped("ak47", ["IIB", "SRI", "S  "], {"I": iron, "B": block, "R": red, "S": stick})
    shaped("sniper", ["  I", "IBI", "SR "], {"I": iron, "B": block, "R": red, "S": stick})
    shaped("rpg", ["IBI", " TS", "  S"], {"I": iron, "B": block, "T": tnt, "S": stick})
    shapeless("pistol_ammo", ["minecraft:copper_ingot", powder], 8)
    shapeless("smg_ammo", ["minecraft:iron_nugget"] * 3 + [powder], 12)
    shapeless("rifle_ammo", [iron, powder], 10)
    shapeless("sniper_ammo", [iron, powder, powder], 5)
    shapeless("rpg_rocket", [tnt, iron, powder], 2)


def gen_lang():
    en = {"itemGroup.hellaverse": "The Boys Hellaverse",
          "message.hellaverse.no_dimension": "This dimension is not available."}
    pt = {"itemGroup.hellaverse": "The Boys Hellaverse",
          "message.hellaverse.no_dimension": "Esta dimensão não está disponível."}
    for ent, label in (("homelander", "Homelander"), ("kimiko", "Kimiko"), ("web_weaver", "Web Weaver"),
                        ("adam", "Adam"), ("soldier_boy", "Soldier Boy")):
        en[f"entity.{MOD}.{ent}"] = label
        pt[f"entity.{MOD}.{ent}"] = label
        en[f"item.{MOD}.{ent}_spawn_egg"] = f"{label} Spawn Egg"
        pt[f"item.{MOD}.{ent}_spawn_egg"] = f"Ovo de Geração: {label}"
    for wid, (w_en, w_pt) in WEAPON_NAMES.items():
        en[f"item.{MOD}.{wid}"] = w_en
        pt[f"item.{MOD}.{wid}"] = w_pt
    en[f"tooltip.{MOD}.gun_ammo"] = "Ammo: %s"
    pt[f"tooltip.{MOD}.gun_ammo"] = "Munição: %s"
    en[f"entity.{MOD}.black_noir"] = "Black Noir"
    pt[f"entity.{MOD}.black_noir"] = "Black Noir"
    en[f"item.{MOD}.black_noir_spawn_egg"] = "Black Noir Spawn Egg"
    pt[f"item.{MOD}.black_noir_spawn_egg"] = "Ovo de Geração: Black Noir"
    for r in RINGS:
        en[f"block.{MOD}.{r['id']}_soil"] = r["soil_en"]
        pt[f"block.{MOD}.{r['id']}_soil"] = r["soil_pt"]
        en[f"item.{MOD}.{r['id']}_key"] = f"Key: {r['en']}"
        pt[f"item.{MOD}.{r['id']}_key"] = f"Chave: {r['pt']}"
    write_json(f"{ASSETS}/lang/en_us.json", en)
    write_json(f"{ASSETS}/lang/pt_br.json", pt)


# ----------------------------------------------------------------------------- dados de mundo
def gen_dimension_type(ring):
    obj = {
        "ultrawarm": False, "natural": True, "coordinate_scale": 1.0,
        "has_skylight": True, "has_ceiling": False, "ambient_light": 0.0,
        "monster_spawn_light_level": 15 if ring["dark"] else
        {"type": "minecraft:uniform", "value": {"min_inclusive": 0, "max_inclusive": 7}},
        "monster_spawn_block_light_limit": 0, "piglin_safe": False, "bed_works": True,
        "respawn_anchor_works": False, "has_raids": False,
        "logical_height": 384, "min_y": -64, "height": 384,
        "infiniburn": "#minecraft:infiniburn_overworld", "effects": ring["effects"],
    }
    if ring["fixed_time"] is not None:
        obj["fixed_time"] = ring["fixed_time"]
    write_json(f"{DATA}/dimension_type/{ring['id']}.json", obj)


def gen_dimension(ring):
    write_json(f"{DATA}/dimension/{ring['id']}.json", {
        "type": f"{MOD}:{ring['id']}",
        "generator": {
            "type": f"{MOD}:ring",
            "settings": "minecraft:overworld",
            "soil": f"{MOD}:{ring['id']}_soil",
            "biome_source": {"type": "minecraft:fixed", "biome": f"{MOD}:{ring['id']}"},
        },
    })


def gen_biome(ring):
    spawners = {k: [] for k in ("monster", "creature", "ambient", "axolotls",
                                "underground_water_creature", "water_creature",
                                "water_ambient", "misc")}
    spawners["monster"] = ring["monsters"]
    if not ring["dark"]:  # paraiso: so animais pacificos
        spawners["creature"] = [
            {"type": "minecraft:sheep", "weight": 12, "minCount": 4, "maxCount": 4},
            {"type": "minecraft:pig", "weight": 10, "minCount": 4, "maxCount": 4},
            {"type": "minecraft:chicken", "weight": 10, "minCount": 4, "maxCount": 4},
            {"type": "minecraft:cow", "weight": 8, "minCount": 4, "maxCount": 4},
            {"type": "minecraft:horse", "weight": 5, "minCount": 2, "maxCount": 6},
        ]
    steps = [[] for _ in range(11)]
    steps[6] = list(ORES)
    for feature in ring["features"]:
        steps[FEATURE_STEPS[feature]].append(feature)
    write_json(f"{DATA}/worldgen/biome/{ring['id']}.json", {
        "has_precipitation": not ring["dark"],
        "temperature": 0.8, "downfall": 0.4,
        "effects": {"sky_color": ring["sky"], "fog_color": ring["fog"],
                    "water_color": ring["water"], "water_fog_color": ring["water_fog"]},
        "spawners": spawners, "spawn_costs": {},
        "carvers": {"air": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"]},
        "features": steps,
    })


def gen_features():
    for name, chance in (("volcano", 6), ("farm", 5)):
        write_json(f"{DATA}/worldgen/configured_feature/{name}.json",
                   {"type": f"{MOD}:{name}", "config": {}})
        write_json(f"{DATA}/worldgen/placed_feature/{name}.json", {
            "feature": f"{MOD}:{name}",
            "placement": [{"type": "minecraft:rarity_filter", "chance": chance},
                          {"type": "minecraft:in_square"},
                          {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
                          {"type": "minecraft:biome"}],
        })


def gen_tags():
    soils = [f"{MOD}:{r['id']}_soil" for r in RINGS]
    for tag in ("mineable/shovel", "stone_ore_replaceables",
                "deepslate_ore_replaceables", "overworld_carver_replaceables"):
        write_json(f"{VANILLA_DATA}/tags/blocks/{tag}.json", {"replace": False, "values": soils})


# Personagens com skin propria (textures/entity/<nome>.png e mantida a mao, nao e gerada aqui).
CHARACTERS = ("black_noir", "homelander", "kimiko", "web_weaver", "adam", "soldier_boy")


def gen_characters():
    for name in CHARACTERS:
        write_json(f"{ASSETS}/models/item/{name}_spawn_egg.json",
                   {"parent": "minecraft:item/template_spawn_egg"})


def main():
    gen_characters()
    gen_weapons()
    for ring in RINGS:
        gen_block_and_item_assets(ring)
        gen_dimension_type(ring)
        gen_dimension(ring)
        gen_biome(ring)
    gen_features()
    gen_tags()
    gen_lang()
    save_png(pentagram_texture(), f"{ASSETS}/textures/environment/pentagram.png")
    print(f"OK: {len(RINGS)} dimensoes geradas.")


if __name__ == "__main__":
    main()
