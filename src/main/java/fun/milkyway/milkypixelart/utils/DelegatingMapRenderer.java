package fun.milkyway.milkypixelart.utils;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A MapRenderer that delegates rendering to another MapView's renderers.
 * This allows rendering the original map's content onto a preview MapView
 * while keeping tracking disabled on the preview.
 */
public class DelegatingMapRenderer extends MapRenderer {
    
    private final MapView sourceView;
    private final List<MapRenderer> sourceRenderers;

    public DelegatingMapRenderer(@NotNull MapView sourceView) {
        super(false);
        this.sourceView = sourceView;
        // Capture renderers at construction time
        this.sourceRenderers = List.copyOf(sourceView.getRenderers());
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        // Call original renderers with the SOURCE MapView (which has the pixel data)
        // but they render to OUR canvas
        for (MapRenderer renderer : sourceRenderers) {
            renderer.render(sourceView, canvas, player);
        }
        
        // Clear any cursors that the original renderers might have added
        clearCursors(canvas.getCursors());
    }
    
    private void clearCursors(@NotNull MapCursorCollection cursors) {
        while (cursors.size() > 0) {
            cursors.removeCursor(cursors.getCursor(0));
        }
    }
}
