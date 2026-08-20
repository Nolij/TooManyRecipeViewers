package dev.nolij.toomanyrecipeviewers.util;

import mezz.jei.api.gui.drawable.TilingDirection;

public record FluidRendererParameters(long capacity, boolean showCapacity, int width, int height, TilingDirection tilingDirection) {}
