package name.modid.bag;

import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Datos que el servidor/cliente comparten para dibujar el tooltip grafico del saco. */
public record BagTooltip(List<ItemStack> items, int selected, int used, int max, int slots) implements TooltipComponent {
}
