# Prompt para continuar el proyecto (copiar y pegar en otra IA)

Sos mi asistente de programacion. Continua el mod de Minecraft "Better Minecraft" (**Fabric**, Minecraft **26.3**).
Respondeme en espanol rioplatense. NO me hagas preguntas de confirmacion: decidi, implementa y
explicame despues que hiciste y que podria fallar. Yo pruebo y te cuento.
El proyecto es solo Fabric: no uses ni me pidas instalar NeoForge/Forge (solo se lo lee como referencia).

Te adjunto el zip del proyecto (`better-minecraft-etapa16.zip`). Descomprimilo y trabaja sobre ese codigo.

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
**Etapa 1 (compila y funciona):** `LapisTiers` + `mixin/EnchantmentMenuMixin`: re-roll de la mesa (3 lapis = vanilla; 4+ = hojas 2..20;
nivel requerido = vanilla + hoja-1; se gastan `hoja + 2` niveles y lapis).

**Etapa 2 (compila y funciona, probado en juego):** `dragon/*`, `world/*`, `beacon/*`: dragon en dos fases con eventos de Fabric,
10 pilares nuevos, isla grande, secuencia de muerte al 50 %, 10 End Crystals, fase 2 (dano x1.5 y ataque Wither), Beacon quita Wither en el End.

**Etapa 3/4 (compila y funciona, probado en juego):** `client/mixin/EnchantmentScreenMixin` (costo real en la UI de la mesa) y cazuela de pociones
(`potion/*`): pocion con efectos sobre caldero = mezcla de efectos (gana mayor nivel, luego mayor duracion); botella de vidrio = pocion con todos los efectos.

**Etapa 5 (compila y funciona, probado en juego): visuales y niveles de la cazuela**
- `PotionCauldronBlock` ahora tiene `LEVEL` (1..3 = cuantas botellas rinde) y `COLOR` (indice 0..15 de `PotionColors`) en el BlockState,
  y `animateTick` con particulas `ENTITY_EFFECT` del color del liquido.
- `PotionColors`: paleta fija de 16 colores (0 = violeta de mezcla). `nearest(rgb)` elige el mas cercano al color del efecto (`MobEffect.getColor()`).
- `PotionCauldronBlockEntity`: guarda ademas `pours` (cuantas pociones se volcaron). `setEffects` fue reemplazado por `setMix(effects, pours)`.
- `PotionCauldronInteraction`: cada pocion volcada sube 1 nivel (max 3; el agua cuenta: caldero con agua nivel 2 + 1 pocion = 3). 2+ pociones volcadas
  (o 2+ efectos distintos) = liquido violeta; 1 sola = color de su efecto. Cada botella baja 1 nivel; al llegar a 0 queda caldero vacio.
- Blockstate `potion_cauldron.json`: multipart por `level` reutilizando los modelos vanilla `minecraft:block/water_cauldron_level1`,
  `..._level2`, `..._full` (el liquido usa tintindex 0).
- `client/BetterMinecraftClient`: `BlockColorRegistry.register(List.of(new BlockTintSource(){ colorInWorld, color }), PotionMod.POTION_CAULDRON)`
  tine el liquido con el color del BlockState (la API se tomo de la doc de Fabric 26.1.2; imports `net.minecraft.client.color.block.BlockTintSource`,
  `net.minecraft.client.renderer.block.BlockAndTintGetter`, `net.minecraft.util.ARGB`).
- Apuestas sin verificar contra 26.3: `ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, int)`, `Block.animateTick(BlockState, Level, BlockPos, RandomSource)`,
  `MobEffect.getColor()`, `LayeredCauldronBlock.LEVEL`, nombres de los modelos vanilla de caldero con agua, y que las clases de tint no cambiaron de paquete en 26.3.

**Etapa 16 (SIN probar; el usuario probara las etapas 15+16 juntas): pocion en la cazuela solo mezcla**
- Se ELIMINARON de la cazuela la polvora (arrojable) y la redstone (+duracion): esas funciones son solo de la destiladora (`BrewingStandMixing`). La cazuela solo mezcla efectos
  (se puede volcar pociones normales y arrojadizas; la botella de vidrio siempre entrega `Items.POTION`). Se quito `splash` del `PotionCauldronBlockEntity`.
- Estado: el mod podria estar terminado; falta probar etapas 15+16 en juego. Ideas opcionales: config en archivo, item de bloque (pick-block), tests en multijugador.

**Etapa 15 (SIN probar): destiladora, ajustes finales**
- La etapa 14 compilo y la animacion de vanilla funciona (probado). Cambios: (1) ya NO se muestran carteles en la action bar en la destiladora (los de la cazuela siguen);
  (2) regla nueva: si UN efecto de la pocion ya esta en 16 min, la pocion entera no se puede alargar mas con redstone (ej.: regeneracion 45 s + resistencia al fuego 8 min:
  1a redstone -> 8:45 y 16:00, y ya no se puede mas), asi los efectos terminan en tiempos distintos. Se mantiene +8 / +6 / +4 por refuerzo.

**Etapa 14 (compila y funciona): destiladora**
- La etapa 13 funciono (salen los carteles). Problemas reportados y arreglos: (1) sin animacion -> se escribe por REFLEXION el campo privado `BrewingStandBlockEntity.brewTime`
  (`BREW_TICKS - progress`, 400 ticks como vanilla) para la flecha/burbujas de la GUI; si el campo no existe se loguea un warning. (2) con una pocion vanilla + una mezclada en el mismo soporte,
  vanilla y el mod procesaban ambas y la redstone se aplicaba doble -> ahora el mod NO procesa si hay en las botellas algo que no sea pocion mezclada (`hasForeignBottle`) y avisa.
  (3) redstone progresiva: 1a vez +8 min, 2a +6, luego +4 (tope 16); el contador de refuerzos se guarda en `DataComponents.REPAIR_COST` de la botella (truco sin NBT).
- Si aun falta animacion: probar tambien escribir el campo `fuel`, o hacer Mixin a `BrewingStandBlockEntity.serverTick`.

**Etapa 13 (compila y funciona): destiladora (brewing stand)**
- El cartografo de la etapa 12 FUNCIONA (probado). End Cities OK. Lo unico que falla: `BrewingStandMixing` no convertia las pociones mezcladas (ni polvora ni redstone), causa NO identificada.
- Cambios: tiempo 10 s (`BREW_TICKS = 200`); ademas del escaneo (ahora solo agrega, no borra) se rastrea el soporte en `UseBlockCallback` al abrirlo; mensajes en la action bar
  ("Destiladora: mezclando... / lista / duracion aumentada / no hay pociones mezcladas aplicables") y logs "Soporte ...". Pedir al usuario que diga que mensaje ve o el `latest.log`.
  Sospechosos si dice "no hay pociones mezcladas aplicables": `isMixed` (`PotionContents.potion().isEmpty() && hasEffects()`). Si no sale ningun mensaje: no se rastrea o `canProcess` no corre.
  Plan B robusto si esto falla: Mixin (con `require = 0`) a `PotionBrewing.hasMix`/`mix` para usar el flujo vanilla (combustible, burbujas).

**Etapa 12 (compila y funciona):**
- End Cities aun mas raras: `SPACING = 96`, `CHANCE = 60` %, anfitrion en la mitad central de la celda (ciudades vecinas a >= 48 chunks) y dedupe en memoria (`DONE`).
- F HECHO: `world/CartographerTrades`. `UseEntityCallback` (servidor): si la entidad es `Merchant` y su profesion (leida por REFLEXION: `getVillagerData().profession()` -> `Holder.unwrapKey()` path "cartographer",
  asi no depende del paquete de Villager en 26.3) y aun no tiene la oferta, agrega a `merchant.getOffers()` una `MerchantOffer(ItemCost(EMERALD,16), Optional.of(ItemCost(COMPASS,1)), mapa, 0, 2, 25, 0.2F)`.
  El mapa: `MapItem.create(level, x, z, (byte)2, true, true)` centrado en `FloatingEndCityGenerator.nearestCity(...)`, `MapItem.renderBiomePreviewMap`, `MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.RED_X)`,
  nombre "Mapa de End City flotante". Se agrega la primera vez que el jugador interactua con el cartografo (se guarda con el aldeano). Apuestas: `MerchantOffer` de 7 args, `ItemCost`, `MapItem.create`,
  `addTargetDecoration`, `MapDecorationTypes.RED_X`, `UseEntityCallback`. Los trades de vanilla 26.1 son data-driven (`villager_trade` / `trade_set`) pero no se pueden apuntar a End Cities propias (no son una Structure registrada).
- Pendiente: soporte para pociones (redstone +8 min) sigue sin confirmarse que funcione; revisar `latest.log` ("Soporte ..."). La polvora/arrojable ya no es prioridad (el usuario dijo que no hace falta).

**Etapa 11:**
- Etapa 10 compilo pero el soporte para pociones NO hacia nada en el juego (ni polvora ni redstone). Se rehizo `BrewingStandMixing`: ya no depende de
  `ServerBlockEntityEvents`; cada 20 ticks escanea los chunks a +-4 de cada jugador (`getChunkNow` + `LevelChunk.getBlockEntities()`) y rastrea los soportes.
  Hay logs INFO: "empezo a procesar pociones mezcladas" (funciona) o "ingrediente X pero ninguna botella aplicable; slot0=... contenido=..." (la deteccion
  `isMixed` falla: pedirle el `latest.log`; sospechoso: `PotionContents.potion().isEmpty()`). Redstone: cada botella se procesa por separado y cada efecto sube +8 min
  con tope 16 (10 min -> 16). Los que estan en el tope no cambian; si ninguna botella cambia no se gasta.
- End Cities mas raras: `SPACING = 80`, `CHANCE = 85` % (como mansiones). `FloatingEndCityGenerator.nearestCity(seed, x, z)` devuelve la mas cercana (determinista).

**Etapa 10:**
- End Cities: probado en juego, funcionan. Se bajo la frecuencia: `SPACING = 56` chunks y `CHANCE = 65` % por celda (antes 20 y 100 %).
- MALENTENDIDO RESUELTO: el usuario con "estante de pociones" se refiere al SOPORTE PARA POCIONES (brewing stand), no a la cazuela. Las pociones mezcladas de la cazuela
  no tienen pocion base, asi que las recetas de vanilla no las reconocen. Nuevo `potion/BrewingStandMixing` (sin Mixins): rastrea `BrewingStandBlockEntity` con
  `ServerBlockEntityEvents.BLOCK_ENTITY_LOAD/UNLOAD`, y en `END_LEVEL_TICK` corre un temporizador propio de 20 s (no usa combustible): polvora en el slot ingrediente (3) =
  botellas mezcladas pasan a `SPLASH_POTION` con los mismos efectos; redstone = +8 min a cada efecto, tope 16 min; si nada cambia no se gasta ni corre.
  "Mezclada" = `PotionContents.potion().isEmpty() && hasEffects()` (apuestas: `potion()`, `hasEffects()`, `BlockEntity.isRemoved()`, eventos de Fabric `ServerBlockEntityEvents`).
  Las pociones de vanilla no se tocan. La polvora/redstone directa sobre la cazuela (etapa 6) se mantiene.
- Ideas: usar combustible (polvo de blaze), animacion de burbujas, config en archivo.

**Etapa 9: la 8 fallo por `MinecraftServer.getStructureManager()` (no existe en 26.3; ahora se obtiene el `StructureTemplateManager` por reflexion en `FloatingEndCityGenerator.templateManager`) y por `new ChunkPos(BlockPos)` (ChunkPos es record: `new ChunkPos(int,int)`).

**Etapa 6/7/8 (etapa 7 NO compilaba: `Player.displayClientMessage` no existe; en la 8 se uso `ServerPlayer.sendSystemMessage(Component, true)` (apuesta). Antes: el usuario reporta que polvora y redstone NO hacen nada en el juego; en la etapa 7 se agregaron mensajes en la action bar y un log al arrancar (`etapa 6 (polvora/redstone...`) para diagnosticar; pedirle `latest.log` y que diga que mensaje ve): polvora y redstone en la cazuela**
- Polvora sobre la cazuela de pociones: `PotionCauldronBlockEntity.splash = true` (se guarda con `putBoolean`/`getBooleanOr`, apuesta: `getBooleanOr`);
  la botella de vidrio entrega `Items.SPLASH_POTION` en vez de `Items.POTION`. Si ya era arrojable no se consume.
- Redstone: +8 min (9600 ticks) a cada efecto, tope 16 min (19200 ticks). No toca efectos instantaneos (duracion <= 1) ni infinitos; los que ya estan
  en el tope no suben. Si ninguno puede subir, no se consume la redstone. Se rearma cada efecto con `new MobEffectInstance(holder, duracion, amplificador)`.
- Todo en `PotionCauldronInteraction`; el estado extra vive en el BlockEntity (no cambia el BlockState).

## Tareas pendientes (en este orden)
A. **Verificar etapa 16**: si Actions falla, te pego el error; corregilo contra las fuentes de verdad. Si el juego se cierra al abrir: `latest.log`
   (buscar `Mixin apply failed` / `InvalidInjectionException`). 
C. **End Cities flotantes (etapa 8, SIN probar, no se si compila)**: `world/FloatingEndCityGenerator`. Sin Mixins ni Structure propia: en
   `ServerChunkEvents.CHUNK_LOAD (level, chunk, newlyGenerated)` (apuesta: la firma con 3 parametros) se detecta el chunk "anfitrion" de cada celda de 20x20 chunks
   (hash determinista con la semilla), se encola, y en `END_LEVEL_TICK` se carga el area, se arma una isla de End Stone (Y 185..215, radio 26..33, cono invertido)
   y se llama `EndCityPieces.startHouseTower(StructureTemplateManager, BlockPos, Rotation, List<StructurePiece>, RandomSource)` (firma tomada de mappings.dev 1.21.11,
   NO verificada en 26.3) y `piece.postProcess(level, level.structureManager(), generator, random, box, chunkPos, pivot)` por cada pieza (asi vienen End Ship,
   Shulkers, Elytra y cofres). Apuestas: `ServerLevel.getSeed()`, `MinecraftServer.getStructureManager()`, `BoundingBox.minX()`, `ChunkPos.getMiddleBlockX()`.
   Si no aparecen ciudades: buscar en `latest.log` "End City flotante generada" o el error; posibles mejoras: persistir celdas hechas, mas rareza, mas variedad de islas.
   Si el usuario sube el artefacto `decompiled-sources` (EndCityPieces, EndCityStructure) verificar las firmas contra el.
D2. Pendientes menores de la cazuela: item de bloque (pick-block no da nada), tests en multijugador.
E. Mejoras opcionales: config en archivo, mensajes al jugador, balance.

## Reglas
- Todo gameplay del lado del servidor, que funcione en multijugador.
- No modificar globalmente Beacons, Wither, End Crystals ni la regeneracion/XP de muerte del dragon.
- Mantener las clases encapsuladas como estan. No crear Mixins innecesarios; preferir eventos de Fabric.
- Antes de usar un nombre de vanilla, buscalo en las fuentes de verdad. Si no lo podes verificar, decime que es una apuesta.
- Cada vez que termines una etapa: resumi como funciona y que podria fallar, y dame el zip completo.
