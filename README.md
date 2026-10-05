# Better Minecraft (Fabric 26.3)

Mod de Minecraft para Fabric (mod id `better-minecraft`, paquete `name.modid`). Todo el gameplay corre del lado del servidor y funciona en multijugador.

## Mesa de encantamientos
- 3 lapis o menos: vanilla puro.
- 4 lapis = hoja 2, ... 22 lapis = hoja 20 (tope). Cada hoja cambia todo el catalogo de encantamientos.
- Nivel requerido de cada fila = valor vanilla + (hoja - 1).
- Se gastan tantos niveles de XP y lapis como `hoja + 2`.
- La interfaz muestra el costo real (lapis y niveles) cuando hay 4 o mas lapis.

## Ender Dragon en dos fases (solo en el End, sin Mixins)
- Fase 1: vanilla. Al comenzar la pelea se construyen 10 pilares nuevos y se agranda la isla.
- 50 % de vida: transicion unica (sonido y particulas de muerte, 5 tandas de XP = mitad de la XP vanilla, invulnerable 10 s).
- Fase 2: aparecen 10 End Crystals nuevos, el dragon hace mas dano a los jugadores (x1.5) y lanza ataques de Wither.
- Beacon activo en el End: quita el efecto Wither a los jugadores dentro de su rango.
- Los numeros de balance estan en `dragon/DragonConfig.java`.

## Cazuela de pociones
- Una pocion con efectos sobre un caldero (vacio, con agua o de pociones) mezcla los efectos (gana el nivel mas alto y, a igualdad, la mayor duracion).
- Cada pocion que se vuelca sube un nivel del caldero (maximo 3). Una botella de vidrio saca una pocion con todos los efectos y baja un nivel.
- El liquido se ve con el color de la pocion (violeta si se mezclaron 2 o mas) y tiene particulas.

## Destiladora (soporte para pociones)
Solo para las pociones mezcladas de la cazuela (las de vanilla siguen con las recetas normales; no mezclar ambas en la misma destiladora):
- **Polvora** en el ingrediente: la pocion pasa a ser arrojable.
- **Redstone** en el ingrediente: suma tiempo a cada efecto. Primera vez +8 min, segunda +6 min, luego +4 min. Maximo 16 minutos.
- En cuanto un efecto llega a 16 minutos, la pocion entera ya no se puede alargar mas (asi los efectos terminan en tiempos distintos).
- Usa la animacion de vanilla (20 segundos); no gasta polvo de blaze.

## End Cities flotantes en el Overworld
- End Cities de vanilla (con End Ships, Shulkers, Elytra y cofres) sobre islas flotantes de End Stone, entre Y 185 y 215.
- Muy raras: una por zona de unos 1500 bloques, y no todas las zonas tienen. Solo aparecen en chunks nuevos.
- Los aldeanos cartografos venden un "Mapa de End City flotante" (16 esmeraldas + 1 brujula) que marca la mas cercana.

## Sacos mejorados
Versiones grandes del bundle, misma interaccion (clic derecho), y la mejora conserva el contenido:
- **Saco de oro**: 8 lingotes de oro alrededor + bundle al centro. Capacidad 70.
- **Saco de hierro**: 8 lingotes de hierro alrededor + saco de oro al centro. Capacidad 128.
- **Saco de hierro mejorado**: bloques de hierro en las filas 1 y 3; fila 2 = bundle, saco de hierro, bundle. 18 stacks (2 x 9, como un shulker mas chico).
- No se pueden meter sacos, bundles ni shulkers adentro.
- Interaccion como el bundle de vanilla: tooltip grafico (grilla de items + barra de capacidad), la **rueda del mouse** sobre el saco
  (en el inventario normal y en el creativo) elige que stack sale, y el **clic derecho en el aire suelta UN stack** (el seleccionado o el ultimo que pusiste).

## Notas de desarrollo
- Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25, sin mappings (nombres de Mojang).
- Se compila con GitHub Actions. Estado y pendientes: ver `HANDOFF_PROMPT.md`.
