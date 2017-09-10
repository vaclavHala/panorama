package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import java.util.Map.Entry;
import static java.util.Objects.requireNonNull;

public class PopMenu {

    private static final float OPEN_TIME = 0.3F;
    private static final Interpolation OPEN_INTERPOL = Interpolation.pow2Out;

    private static final float CLOSE_TIME = 0.2F;
    private static final Interpolation CLOSE_INTERPOL = Interpolation.pow2In;

    private static final float SNAP_BIAS = 2.5F;

    /**
     * Two columns, inner one (left if this is the right side dispaly and vice versa)
     * is empty in all but the lowest cell where the flip arrow is
     */
    private Table table;

    private MenuState state;
    private MenuSide side;
    private float handleWidth;
    private float buttonWidth;

    private float posDisabled;
    private float posClosed;
    private float posOpen;

    /**
     * @param buttons [icon, action]
     */
    public PopMenu(
            Skin skin, MenuSide side,
            float stageWidth, float stageHeight,
            java.util.List<Entry<String, Runnable>> buttons) {
        this.side = side;
        handleWidth = 30;
        float handleHeight = 50;
        buttonWidth = 70;
        float buttonHeight = (stageHeight - handleHeight) / buttons.size();
        table = new Table(skin);
        table.setRound(false);

        this.side.config(this, stageWidth, stageHeight);

        this.state = MenuState.CLOSED;

        for (Entry<String, Runnable> btnDef : buttons) {
            Button button = this.side.addRegularButton(this, skin, btnDef.getKey(), buttonHeight);
            final Runnable btnAction = requireNonNull(btnDef.getValue(), "btn action");
            button.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    btnAction.run();
                }
            });
        }

        Actor handle = side.addHandle(this, skin, handleHeight);
        handle.addListener(new PopMenuListener(this));

        table.pack();
    }

    public Actor actor() {
        return table;
    }

    public void enable() {
        state = MenuState.CLOSED;
        table.addAction(moveTo(posClosed, 0, CLOSE_TIME, CLOSE_INTERPOL));
    }

    public void disable() {
        state = MenuState.DISABLED;
        table.addAction(moveTo(posDisabled, 0, CLOSE_TIME, CLOSE_INTERPOL));
    }

    private static class PopMenuListener extends DragListener {

        private static final float MAX_TAP_DURATION = 100;

        private final PopMenu menu;
        private float originActor;
        private long tapStart;

        public PopMenuListener(PopMenu menu) {
            this.menu = menu;
            this.setTapSquareSize(0);
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            this.tapStart = System.currentTimeMillis();
            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);

            long time = System.currentTimeMillis() - this.tapStart;
            if (time < MAX_TAP_DURATION) {
                if (menu.state == MenuState.OPEN) {
                    menu.state = MenuState.CLOSED;
                    menu.table.addAction(moveTo(menu.posClosed, 0, CLOSE_TIME, CLOSE_INTERPOL));
                } else if (menu.state == MenuState.CLOSED) {
                    menu.state = MenuState.OPEN;
                    menu.table.addAction(moveTo(menu.posOpen, 0, OPEN_TIME, OPEN_INTERPOL));
                }
            }
        }

        @Override
        public void drag(InputEvent event, float x, float y, int pointer) {
            if (menu.state == MenuState.DISABLED) {
                return;
            }
            float delta = event.getStageX() - getStageTouchDownX();
            float pos = menu.side.clampPosition(this.originActor + delta, menu.posClosed, menu.posOpen);

            menu.table.setX(pos);
        }

        @Override
        public void dragStart(InputEvent event, float x, float y, int pointer) {
            this.originActor = menu.table.getX();
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer) {
            if (menu.state == MenuState.DISABLED) {
                return;
            }
            float distOpen = Math.abs(menu.table.getX() - menu.posOpen);
            float distClosed = Math.abs(menu.table.getX() - menu.posClosed);
            float relDist = Math.min(distOpen, distClosed) / menu.buttonWidth;
            relDist = relDist < 0 ? 0 : relDist > 1 ? 1 : relDist;
            // if we are opening we want to bias the area from which we snap to open and vice versa
            if (menu.state == MenuState.OPEN) {
                distOpen *= SNAP_BIAS;
            } else {
                distClosed *= SNAP_BIAS;
            }
            if (distOpen < distClosed) {
                menu.state = MenuState.OPEN;
                menu.table.addAction(moveTo(menu.posOpen, 0, OPEN_TIME * relDist, OPEN_INTERPOL));
            } else {
                menu.state = MenuState.CLOSED;
                menu.table.addAction(moveTo(menu.posClosed, 0, CLOSE_TIME * relDist, CLOSE_INTERPOL));
            }
        }

    }

    public static enum MenuSide {
        LEFT {

            @Override
            void config(PopMenu menu, float stageWidth, float stageHeight) {
                menu.posClosed = 0 - menu.buttonWidth;
                menu.posOpen = menu.posClosed + menu.buttonWidth;
                menu.posDisabled = 0 - menu.buttonWidth - menu.handleWidth;

                menu.table.setPosition(menu.posClosed, 0);
            }

            @Override
            Button addRegularButton(PopMenu menu, Skin skin, String buttonIcon, float buttonHeight) {
                ImageButtonStyle baseStyle = skin.get(ImageButtonStyle.class);
                ImageButtonStyle style = new ImageButtonStyle(baseStyle);
                //left right
                style.imageUp = skin.getDrawable(buttonIcon);
                Button button = new ImageButton(style);
                menu.table.add(button).width(menu.buttonWidth).height(buttonHeight);

                menu.table.add().width(menu.handleWidth);
                menu.table.row();
                return button;
            }

            @Override
            Actor addHandle(PopMenu menu, Skin skin, float handleHeight) {
                Actor handle = new TextButton("<>", skin);
                menu.table.add(handle).width(menu.handleWidth + menu.buttonWidth).height(handleHeight).colspan(2);
                menu.table.row();
                return handle;
            }

            @Override
            float clampPosition(float pos, float posClosed, float posOpen) {
                pos = pos > posOpen ? posOpen : pos;
                pos = pos < posClosed ? posClosed : pos;
                return pos;
            }

        },
        RIGHT {

            @Override
            void config(PopMenu menu, float stageWidth, float stageHeight) {
                menu.posClosed = stageWidth - menu.handleWidth;
                menu.posOpen = menu.posClosed - menu.buttonWidth;
                menu.posDisabled = stageWidth;

                menu.table.setPosition(menu.posClosed, 0);
            }

            @Override
            Button addRegularButton(PopMenu menu, Skin skin, String buttonIcon, float buttonHeight) {
                menu.table.add().width(menu.handleWidth);
                ImageButtonStyle baseStyle = skin.get(ImageButtonStyle.class);
                ImageButtonStyle style = new ImageButtonStyle(baseStyle);
                //left right
                style.imageUp = skin.getDrawable(buttonIcon);
                Button button = new ImageButton(style);
                menu.table.add(button).width(menu.buttonWidth).height(buttonHeight);
                menu.table.row();
                return button;
            }

            @Override
            Actor addHandle(PopMenu menu, Skin skin, float handleHeight) {
                Actor handle = new TextButton("<>", skin);
                menu.table.add(handle).width(menu.handleWidth + menu.buttonWidth).height(handleHeight).colspan(2);
                menu.table.row();
                return handle;
            }

            @Override
            float clampPosition(float pos, float posClosed, float posOpen) {
                pos = pos < posOpen ? posOpen : pos;
                pos = pos > posClosed ? posClosed : pos;
                return pos;
            }

        };

        abstract void config(PopMenu menu, float stageWidth, float stageHeight);

        abstract Button addRegularButton(PopMenu menu, Skin skin, String buttonIcon, float buttonHeight);

        abstract Actor addHandle(PopMenu menu, Skin skin, float handleHeight);

        abstract float clampPosition(float pos, float posClosed, float posOpen);
    }

    private static enum MenuState {
        CLOSED,
        OPEN,
        DISABLED
    }

}
