package name.modid.client;

import java.util.List;
import name.modid.bag.BagTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Tooltip grafico del saco, como el del bundle de vanilla: grilla de items, seleccion y barra de capacidad. */
public class ClientBagTooltip implements ClientTooltipComponent {
	private static final int CELL = 18;
	private static final int BAR_HEIGHT = 6;

	private final BagTooltip data;
	private final int cols;
	private final int rows;
	private final int cells;

	public ClientBagTooltip(BagTooltip data) {
		this.data = data;
		if (data.slots() > 0) {
			this.cols = 6;
			this.cells = data.slots();
		} else {
			this.cols = 4;
			int needed = Math.max(1, data.items().size() + (data.used() >= data.max() ? 0 : 1));
			this.cells = ((needed + 3) / 4) * 4;
		}
		this.rows = (this.cells + this.cols - 1) / this.cols;
	}

	@Override
	public int getWidth(Font font) {
		return this.cols * CELL;
	}

	@Override
	public int getHeight(Font font) {
		return this.rows * CELL + 4 + BAR_HEIGHT + 3 + font.lineHeight + 2;
	}

	@Override
	public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
		List<ItemStack> items = this.data.items();
		for (int i = 0; i < this.cells; i++) {
			int cx = x + (i % this.cols) * CELL;
			int cy = y + (i / this.cols) * CELL;
			graphics.fill(cx, cy, cx + CELL, cy + CELL, 0xFF373737);
			graphics.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, 0xFF8B8B8B);
			if (i < items.size()) {
				ItemStack stack = items.get(i);
				graphics.item(stack, cx + 1, cy + 1);
				graphics.itemDecorations(font, stack, cx + 1, cy + 1);
			}
		}
		int sel = this.data.selected();
		if (sel >= 0 && sel < items.size()) {
			int cx = x + (sel % this.cols) * CELL;
			int cy = y + (sel / this.cols) * CELL;
			int white = 0xFFFFFFFF;
			graphics.fill(cx, cy, cx + CELL, cy + 1, white);
			graphics.fill(cx, cy + CELL - 1, cx + CELL, cy + CELL, white);
			graphics.fill(cx, cy, cx + 1, cy + CELL, white);
			graphics.fill(cx + CELL - 1, cy, cx + CELL, cy + CELL, white);
		}

		// Barra de capacidad: azul, y roja cuando esta llena.
		int gridWidth = this.cols * CELL;
		int by = y + this.rows * CELL + 4;
		graphics.fill(x, by, x + gridWidth, by + BAR_HEIGHT, 0xFF000000);
		double fraction = this.data.max() <= 0 ? 0.0 : Math.min(1.0, (double) this.data.used() / this.data.max());
		int fillWidth = (int) Math.round((gridWidth - 2) * fraction);
		int color = this.data.used() >= this.data.max() ? 0xFFE04040 : 0xFF4C86E0;
		if (fillWidth > 0) {
			graphics.fill(x + 1, by + 1, x + 1 + fillWidth, by + BAR_HEIGHT - 1, color);
		}
	}

	@Override
	public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		int ty = y + this.rows * CELL + 4 + BAR_HEIGHT + 3;
		List<ItemStack> items = this.data.items();
		int sel = this.data.selected();
		Component line = (sel >= 0 && sel < items.size())
			? items.get(sel).getHoverName()
			: Component.literal(this.data.used() + "/" + this.data.max());
		graphics.text(font, line, x, ty, 0xFFAAAAAA, true);
	}
}
