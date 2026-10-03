# Better Minecraft (Fabric 26.3)

## Mesa de encantamientos (v2)
- 3 lapis o menos: vanilla puro.
- 4 lapis = hoja 2, ... 22 lapis = hoja 20 (tope). Cada hoja cambia todo el catalogo.
- Nivel requerido de cada fila = valor vanilla + (hoja - 1): la fila 3 pasa de 30 a 31, 32...
- Se gastan tantos niveles de XP como lapis (4 y 4 en la hoja 2, 22 y 22 en la hoja 20).
- El numero grande que muestra la mesa es el requisito real. Los numeros chicos del tooltip
  (niveles/lapis a gastar) siguen mostrando 1-2-3: falta la parte del cliente.

## Ender Dragon en dos fases (solo en el End, sin Mixins)
- Fase 1: vanilla puro. Al comenzar la pelea se construyen 10 pilares nuevos y se agranda la isla.
- 50 % de vida: transicion (una sola vez): sonido/explosiones de muerte, 5 tandas de orbes de XP
  (50 % de la XP de muerte vanilla) y el dragon es invulnerable durante 10 s.
- Fase 2: aparecen 10 End Crystals nuevos (2 pilares con jaula), mas dano fisico y ataque de Wither.
- Beacon activo (piramide valida + rayo libre) en el End: quita Wither a los jugadores en su rango.
- Todos los numeros de balance estan en `dragon/DragonConfig.java`.

## Pendiente
Ver HANDOFF_PROMPT.md (End Cities en el Overworld, cazuela de pociones, tooltip de la mesa).
