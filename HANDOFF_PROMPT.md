# Prompt para continuar el proyecto (copiar y pegar en otra IA)

Sos mi asistente de programacion. Continua el mod de Minecraft "Better Minecraft" (**Fabric**, Minecraft **26.3**).
Respondeme en espanol rioplatense. NO me hagas preguntas de confirmacion: decidi, implementa y
explicame despues que hiciste y que podria fallar. Yo pruebo y te cuento.
El proyecto es solo Fabric: no uses ni me pidas instalar NeoForge/Forge (solo se lo lee como referencia).

Te adjunto el zip del proyecto (`better-minecraft-etapa3b.zip`). Descomprimilo y trabaja sobre ese codigo.

## Entorno (verificado)
- Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.18-SNAPSHOT, Java 25.
- **Sin mappings**: desde 26.1 el juego no esta ofuscado. Se usan los nombres de Mojang
  (`ServerLevel`, `BaseEntityBlock`, `Identifier`, `hurtServer(...)`, etc.). NO uses nombres Yarn.
- Paquete `name.modid`, mod id `better-minecraft`. No se compila en local: se sube a GitHub y
  GitHub Actions compila (el tilde verde = compilo). El jar esta en el artefacto `Artifacts`
  (usar el que NO termina en `-sources`). El workflow tambien exporta el codigo fuente del juego
  como artefacto `decompiled-sources` (EnchantmentMenu, EnchantmentScreen, EnderDragon, EnderDragonFight,
  EndCityPieces, EndCityStructure, EntityBlock, etc.). Si te lo subo, es la mejor fuente de verdad.
- Mixins con `defaultRequire: 1`: si un objetivo no existe, el juego se cierra al arrancar
  (los mixins puramente esteticos usan `require = 0`).
- MixinExtras (`@WrapOperation`) viene con Fabric Loader.

## Fuentes de verdad para firmas de vanilla 26.3 (no inventes APIs)
1. **Fabric API, rama `26.3`** (`git clone --depth 1 --branch 26.3 https://github.com/FabricMC/fabric-api.git`).
   Los `src/testmod` tienen ejemplos reales y actuales. Ya confirmados ahi:
   - Bloques con BlockEntity: `extends BaseEntityBlock` (NO hace falta `codec()`), `newBlockEntity(BlockPos, BlockState)`,
     `getTicker(...)`, `BlockBehaviour.Properties.of().setId(ResourceKey<Block>)`,
     `FabricBlockEntityTypeBuilder.create(Factory::new, block).build()`, `Registry.register(BuiltInRegistries.BLOCK, key, block)`.
   - BlockEntity: `public void loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`;
     `input.read("k", CODEC)` devuelve `Optional`, `output.store("k", CODEC, value)`, `input.getIntOr("k", 0)`, `output.putInt(...)`.
   - Eventos: `ServerLivingEntityEvents.AFTER_DAMAGE (entity, source, baseDamageTaken, damageTaken, blocked)`, `AFTER_DEATH`,
     `UseBlockCallback.EVENT (player, level, hand, hitResult)`, `ServerTickEvents.END_LEVEL_TICK`.
2. **NeoForge, rama `port/26.3`, carpeta `patches/`** (clonar con `--filter=blob:none --sparse` y `git sparse-checkout set patches`):
   parches sobre clases de vanilla con codigo original como contexto. Solo existen para las clases que NeoForge modifica
   (por ejemplo `EnchantmentScreen`, `Player`, `Entity`, `EnderDragonFight`); NO estan `EndCrystal`, `AreaEffectCloud`,
   `EndCityPieces`, `EntityBlock`, etc. Los archivos terminan en `.java.patch`.
3. La API de busqueda de codigo de GitHub pide autenticacion: no sirve.

## Estado actual
**Etapa 1 (compilo y funciono):**
- `LapisTiers` + `mixin/EnchantmentMenuMixin`: re-roll de la mesa (3 lapis = vanilla; 4+ = hojas 2..20; nivel requerido =
  vanilla + hoja-1; se gastan tantos niveles y lapis como `hoja + 2`). Engancha `lambda$slotsChanged$0` y `getEnchantmentList`.

**Etapa 2 (SIN probar; no se si compila):**
- `dragon/*`, `world/*`: dragon en dos fases con eventos de Fabric (sin Mixins): `DragonFightManager`, `DragonPhaseManager`
  (bits persistentes via Data Attachment API, `ModAttachments`), `DragonConfig`; 10 pilares nuevos (`SecondaryPillarData`,
  `EndPillarGenerator`), isla grande (`EndIslandGenerator`); al 50 %: `DragonDeathSequence` (sonido, particulas, XP en 5 tandas =
  XP vanilla/2 via `DragonExperienceManager`) y `DragonCrystalManager` (10 End Crystals, una vez); fase 2: `DragonDamage`
  (x1.5 a jugadores) y `DragonWitherAttack` (WitherSkull + nube de Wither).
- `beacon/BeaconProtectionManager`: en el End quita Wither a jugadores dentro del rango de un Beacon activo.

**Etapa 3 (SIN probar; nueva):**
- **B** `src/client/.../mixin/EnchantmentScreenMixin`: envuelve `Component.translatable` (selector regex `"/.*/"`, `require = 0`)
  y cambia el numero de `container.enchant.lapis.*` y `container.enchant.level.*` por el costo real (`LapisTiers.lapisCost`)
  cuando hay 4+ lapis. Registrado en `better-minecraft.client.mixins.json`.
- **D** `potion/*`: cazuela de pociones. `PotionMod.init()` (llamado desde `onInitialize`) registra bloque + BlockEntityType;
  `PotionCauldronBlock` (BaseEntityBlock), `PotionCauldronBlockEntity` (guarda efectos con ValueInput/ValueOutput),
  `PotionCauldronInteraction` (por `UseBlockCallback`): pocion con efectos sobre caldero vacio/con agua/de pociones = mezcla los
  efectos (gana mayor nivel, luego mayor duracion); botella de vidrio sobre la cazuela = una pocion con todos los efectos.
  Hay blockstate, lang (en_us, es_ar) y loot table (suelta un caldero).

## Tareas pendientes (en este orden)
A. **Verificar y arreglar etapas 2 y 3**: si Actions falla, te pego el error; corregilo contra las fuentes de verdad.
   Primera compilacion de la etapa 3: fallaron 4 cosas y ya se corrigieron en `etapa3b` (esperando el resultado de Actions):
   `Player.drop(ItemStack, boolean)` no existe (ahora `getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY)`),
   `Entity.invulnerableTime` es privado (ahora reflexion en `DragonDamage`), y `EnderDragon.setInvulnerable(boolean)` no existe
   (ahora se bloquea el dano con `ServerLivingEntityEvents.ALLOW_DAMAGE` durante la transicion).
   Javac puede mostrar errores nuevos despues de corregir estos (aparecen por fases). Dudas que aun no dieron error: tratalas como
   apuestas hasta que compile.
   Si el juego se cierra al abrir: te paso `latest.log` (buscar `Mixin apply failed` / `InvalidInjectionException`).
C. **End Cities en el Overworld sobre islas flotantes de End Stone** (Y >= 180): conservar End Ships, Shulkers y Elytra, y la
   estructura de vanilla (`EndCityPieces`). Un JSON de estructura con tipo `minecraft:end_city` la pone sobre el terreno, no
   flotando. Opciones: `Structure` propia en Java que genere una isla (End Stone) y llame a `EndCityPieces.startHouseTower(...)`,
   o un evento de generacion de chunk. NO escribirlo sin ver `EndCityPieces`/`EndCityStructure`: pedime el artefacto
   `decompiled-sources` de mi Actions.
D2. Pendientes menores de la cazuela: item de bloque (pick-block no da nada), modelo con liquido visible, tests en multijugador.
E. Mejoras opcionales: config en archivo, mensajes al jugador, balance.

## Reglas
- Todo gameplay del lado del servidor, que funcione en multijugador.
- No modificar globalmente Beacons, Wither, End Crystals ni la regeneracion/XP de muerte del dragon.
- Mantener las clases encapsuladas como estan. No crear Mixins innecesarios; preferir eventos de Fabric.
- Antes de usar un nombre de vanilla, buscalo en las fuentes de verdad. Si no lo podes verificar, decime que es una apuesta.
- Cada vez que termines una etapa: resumi como funciona y que podria fallar, y dame el zip completo.
