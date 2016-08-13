package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CompasStrip extends Table {

    private final GlyphWithOffset[] glyphs;

    private final double shiftPerDegPix;

    public CompasStrip(float fov, float glyphSize, TextureAtlas skin) {
        this.shiftPerDegPix = Gdx.graphics.getWidth() / fov;

        String[] glyphNames = {
                "N", "NNE", "NE", "ENE",
                "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW",
                "W", "WNW", "NW", "NNW"
        };
        this.glyphs = new GlyphWithOffset[glyphNames.length];
        double degsPerGlyph = 360.0 / glyphNames.length;
        for (int i = 0; i < glyphNames.length; i++) {
            glyphs[i] = new GlyphWithOffset(new Image(skin.findRegion(glyphNames[i])), i * degsPerGlyph);
            add(glyphs[i].glyph).width(glyphSize*2).height(glyphSize);
        }
    }

    /**
     * @param direction value is in degrees between 0 (north) and goes to 360 (north again) clockwise
     */
    public void update(double direction) {
        for (GlyphWithOffset g : this.glyphs) {
            double rawPos = (g.offsetDeg - direction) * shiftPerDegPix;
            double wrappedPos = rawPos;
            if (rawPos > Gdx.graphics.getWidth() / 2 + g.glyph.getWidth()) {
                wrappedPos = rawPos - 360 * shiftPerDegPix;
            } else if (rawPos < -Gdx.graphics.getWidth() / 2 - g.glyph.getWidth()) {
                wrappedPos = rawPos + 360 * shiftPerDegPix;
            }
            centeredAt(wrappedPos, g.glyph);
        }
    }

    private void centeredAt(double x, Image img) {
        img.setX((float) (getWidth() / 2 + x - img.getWidth() / 2));
    }

    private static class GlyphWithOffset {

        private final Image glyph;
        private final double offsetDeg;

        public GlyphWithOffset(Image glyph, double offsetDeg) {
            this.glyph = glyph;
            this.offsetDeg = offsetDeg;
        }
    }
}
