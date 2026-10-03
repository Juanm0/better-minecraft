package name.modid.bag;

/**
 * Capacidad de cada saco. maxWeight = peso maximo en "unidades de bundle" (64 = un stack de 64; items de stack 16 pesan 4, no apilables 64);
 * maxStacks = cantidad maxima de stacks distintos (0 = sin limite). Un valor 0 significa "no aplica".
 */
public record BagTier(int maxWeight, int maxStacks) {
	public static final BagTier GOLD = new BagTier(70, 0);
	public static final BagTier IRON = new BagTier(128, 0);
	/** Como un cofre/shulker pero de 2 x 9 = 18 stacks. */
	public static final BagTier REINFORCED_IRON = new BagTier(0, 18);
}
