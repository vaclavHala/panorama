package com.mygdx.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import static com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import static com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class Tweaker extends Table {

    public Tweaker(TweakerAction action, String name, float min, float max, float step, Skin skin) {
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = skin.getFont("font");
        Label label = new Label(null, labelStyle);

        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = new NinePatchDrawable(skin.getPatch("default-slider"));
        sliderStyle.knob = new NinePatchDrawable(skin.getPatch("default-slider-knob"));
        Slider slider = new Slider(min, max, step, false, sliderStyle);
        slider.addListener(new TweakerListener(name, label, action));

        label.setText(name+": "+slider.getVisualValue());

        add(slider).width(300).row();
        add(label).left();
    }

    public interface TweakerAction {
        void react(float newValue);
    }

    private static class TweakerListener extends ChangeListener {

        private String name;
        private Label label;
        private TweakerAction action;

        public TweakerListener(String name, Label label, TweakerAction action) {
            this.name = name;
            this.label = label;
            this.action = action;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            Slider changed = (Slider) event.getTarget();
            float value = changed.getVisualValue();
            label.setText(name + ": " + value);
            action.react(value);
        }
    }
}
