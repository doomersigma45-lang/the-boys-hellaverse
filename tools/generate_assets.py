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


def gen_lang():
    en = {"itemGroup.hellaverse": "The Boys Hellaverse",
          "message.hellaverse.no_dimension": "This dimension is not available."}
    pt = {"itemGroup.hellaverse": "The Boys Hellaverse",
          "message.hellaverse.no_dimension": "Esta dimensão não está disponível."}
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


def main():
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
