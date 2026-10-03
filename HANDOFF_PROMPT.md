# Prompt para continuar el proyecto (copiar y pegar en otra IA)

Sos mi asistente de programacion. Continua el mod de Minecraft "Better Minecraft" (Fabric, Minecraft **26.3**).
Respondeme en espanol rioplatense. NO me hagas preguntas de confirmacion: decidi, implementa y
explicame despues que hiciste y que podria fallar. Yo pruebo y te cuento.

## Entorno (verificado)
- Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.18-SNAPSHOT, Java 25.
- **Sin mappings**: desde 26.1 el juego no esta ofuscado. Se usan los nombres de Mojang
  (`ServerLevel`, `EntityBlock`, `Identifier`, `hurtServer(...)`, etc.). NO uses nombres Yarn
  (por ejemplo `BlockEntityProvider` no existe: es `EntityBlock`).
- Paquete `name.modid`, mod id `better-minecraft`. No se compila en local: se sube a GitHub y
  GitHub Actions compila (el tilde verde = compilo). El jar esta en el artefacto `Artifacts`
  (usar el que NO termina en `-sources`). El workflow tambien exporta el codigo fuente del juego
  como artefacto `decompiled-sources` (EnchantmentMenu, EnderDragon, EnderDragonFight, etc.).
- Tu fuente de verdad para firmas de clases de vanilla 26.3 (si no tenes el codigo del juego):
  - https://github.com/FabricMC/fabric-api (rama `26.3`): mixins y tests con nombres reales.
  - https://github.com/neoforged/NeoForge (rama `port/26.3`, carpeta `patches/`): parches sobre
    las clases de vanilla con codigo original como contexto.
  Antes de usar un nombre de vanilla, buscalo ahi. No inventes APIs.
- Mixins con `defaultRequire: 1`: si un objetivo no existe, el juego se cierra al arrancar.
- MixinExtras (`@WrapOperation`) viene con Fabric Loader. Si no compila, agregar la dependencia.

## Estado actual (todo compilo en etapa 1; lo nuevo de la etapa 2 esta SIN probar)
1. `LapisTiers` + `mixin/EnchantmentMenuMixin`: re-roll de la mesa (3 lapis = vanilla; 4+ = hojas
   2..20; nivel requerido = vanilla + hoja-1; se gastan tantos niveles como lapis).
   Engancha `lambda$slotsChanged$0` (nombre confirmado por Fabric API) y `getEnchantmentList`.
2. `dragon/*`, `world/*`: dragon en dos fases (sin Mixins, con eventos de Fabric):
   - `DragonFightManager` (tick por nivel `ServerTickEvents.END_LEVEL_TICK`), `DragonPhaseManager`
     (bits persistentes en el nivel via Fabric Data Attachment API), `DragonConfig` (balance).
   - Terreno: 10 pilares nuevos (`SecondaryPillarData` determinista por semilla, 2 con jaula,
     `EndPillarGenerator`) e isla grande (`EndIslandGenerator`, por tandas).
   - 50 %: `DragonDeathSequence` (sonido + particulas, dragon invulnerable 200 ticks, 5 tandas de orbes
     con `ExperienceOrb.award`, total = XP vanilla/2 via `DragonExperienceManager`), luego
     `DragonCrystalManager.spawnAll` (10 End Crystals, una sola vez).
   - Fase 2: `DragonDamage` (dano x1.5 a jugadores via AFTER_DAMAGE) y `DragonWitherAttack`
     (WitherSkull + AreaEffectCloud de Wither al impactar).
3. `beacon/BeaconProtectionManager`: en el End quita Wither a jugadores dentro del rango de un
   Beacon activo (piramide propia con `BlockTags.BEACON_BASE_BLOCKS`, rayo libre con `canOcclude`).

## Tareas pendientes (en este orden)
A. **Verificar y arreglar la etapa 2**: si Actions falla, leer el error y corregirlo contra las fuentes
   de verdad. Puntos dudosos que no pude comprobar: constructor `new EndCrystal(Level,double,double,double)`,
   `EndCrystal.setShowBottom`, constructor `new WitherSkull(Level,LivingEntity,Vec3)`, `new AreaEffectCloud(Level,x,y,z)`,
   `AreaEffectCloud.setRadius/setDuration/addEffect`, `ServerChunkCache.getChunkNow`, `BlockTags.BEACON_BASE_BLOCKS`,
   `Player.invulnerableTime` (si es privado, usar otra forma), `giveExperienceLevels`, `hasChunkAt`.
B. **Tooltip de la mesa**: el cliente muestra "1/2/3 niveles y lapis" en vez de los reales. Hay que tocar
   `EnchantmentScreen` (cliente) para mostrar niveles y lapis reales (hoja+2). Mixin en `src/client`.
C. **End Cities en el Overworld sobre islas flotantes de End Stone** (Y >= 180): conservar End Ships,
   Shulkers y Elytra, y la estructura de vanilla (`EndCityPieces`). Un JSON de estructura con tipo
   `minecraft:end_city` las pone sobre el terreno, no flotando. Opciones: `Structure` propia en Java que
   genere una isla (End Stone) y llame a `EndCityPieces.startHouseTower(...)`, o un evento de generacion
   de chunk. Verificar firmas en las fuentes de verdad antes de escribir.
D. **Cazuela de pociones** (prototipo viejo en `pending/potion_prototype/`, NO compila): reescribir con
   `EntityBlock`, `BlockBehaviour.Properties.setId(...)`, guardado/carga de efectos en el BlockEntity
   (en 26.x se usa `ValueInput`/`ValueOutput`; confirmarlo), llamar al registro desde `onInitialize`, y
   modelos/blockstate/lang. Idea: mezclar efectos de pociones en un caldero y sacar una pocion con todos.
E. Mejoras opcionales: config en archivo, mensajes al jugador, balance.

## Reglas
- Todo gameplay del lado del servidor, que funcione en multijugador.
- No modificar globalmente Beacons, Wither, End Crystals ni la regeneracion/XP de muerte del dragon.
- Mantener las clases encapsuladas como estan. No crear Mixins innecesarios; preferir eventos de Fabric.
- Cada vez que termines una etapa: resumi como funciona y que podria fallar, y dame el zip completo.
