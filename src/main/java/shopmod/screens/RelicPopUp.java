package shopmod.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import shopmod.relics.MerchantsRug;

public class RelicPopUp {
    private AbstractRelic relic;
    public boolean isHidden = true;
    public boolean targetMode = false;
    private static final float HB_W = 184.0f;
    private static final float HB_H = 52.0f;
    private Hitbox hbTop = new Hitbox(HB_W, HB_H);
    private Hitbox hbBot = new Hitbox(HB_W, HB_H);
    private float x;
    private float y;
    private static final String INFO_LABEL = "Inspect";
    private static final String SELL_LABEL = "Sell";
    public static final String[] SELL_MESSAGE = {"Sell this relic NL Obtain #b", " #ygold"};
    private static final String CANT_SELL_LABEL = "Can't sell";
    public static final String[] CANT_SELL_MESSAGE = {"This relic can not be sold."};

    public void open(AbstractRelic relic) {
        AbstractDungeon.topPanel.selectPotionMode = false;
        this.relic = relic;
        this.x = relic.hb.cX;
        this.y = relic.hb.cY - 144.0f * Settings.scale;
        this.isHidden = false;
        this.hbTop.move(this.x, this.y + 44.0f * Settings.scale);
        this.hbBot.move(this.x, this.y - 14.0f * Settings.scale);
        this.hbTop.clickStarted = false;
        this.hbBot.clickStarted = false;
        this.hbTop.clicked = false;
        this.hbBot.clicked = false;
    }

    public void close() {
        this.isHidden = true;
    }

    public void update() {
        if (!this.isHidden) {
            this.updateControllerInput();
            this.hbTop.update();
            this.hbBot.update();
            this.updateInput();
        }
    }

    private void updateControllerInput() {
        if (!Settings.isControllerMode) {
            return;
        }
        if (CInputActionSet.cancel.isJustPressed()) {
            CInputActionSet.cancel.unpress();
            this.close();
            return;
        }
        if (!this.hbTop.hovered && !this.hbBot.hovered) {
            Gdx.input.setCursorPosition((int)this.hbTop.cX, Settings.HEIGHT - (int)this.hbTop.cY);
        } else if (this.hbTop.hovered) {
            if (CInputActionSet.up.isJustPressed() || CInputActionSet.down.isJustPressed() || CInputActionSet.altUp.isJustPressed() || CInputActionSet.altDown.isJustPressed()) {
                Gdx.input.setCursorPosition((int)this.hbBot.cX, Settings.HEIGHT - (int)this.hbBot.cY);
            }
        } else if (this.hbBot.hovered && MerchantsRug.canSell(relic) && (CInputActionSet.up.isJustPressed() || CInputActionSet.down.isJustPressed() || CInputActionSet.altUp.isJustPressed() || CInputActionSet.altDown.isJustPressed())) {
            Gdx.input.setCursorPosition((int)this.hbTop.cX, Settings.HEIGHT - (int)this.hbTop.cY);
        }
    }

    private void updateInput() {
        if (InputHelper.justClickedLeft) {
            InputHelper.justClickedLeft = false;
            if (this.hbTop.hovered) {
                this.hbTop.clickStarted = true;
                InputHelper.justClickedLeft = false; // Don't propagate click to things below
            } else if (this.hbBot.hovered) {
                this.hbBot.clickStarted = true;
                InputHelper.justClickedLeft = false;
            } else {
                this.close();
            }
        }
        if ((this.hbTop.clicked || this.hbTop.hovered && CInputActionSet.select.isJustPressed())) {
            CInputActionSet.select.unpress();
            this.hbTop.clicked = false;
            CardCrawlGame.relicPopup.open(relic, AbstractDungeon.player.relics);
            CInputActionSet.select.unpress();
            InputHelper.justClickedLeft = false;
            this.close();
        } else if ((this.hbBot.clicked || this.hbBot.hovered && CInputActionSet.select.isJustPressed()) && MerchantsRug.canSell(relic)) {
            CInputActionSet.select.unpress();
            this.hbBot.clicked = false;
            MerchantsRug.sell(relic);
            CInputActionSet.select.unpress();
            InputHelper.justClickedLeft = false;
            this.close();
        }
        if (this.hbTop.hovered) {
            CardCrawlGame.cursor.changeType(GameCursor.CursorType.INSPECT);
        }
    }

    public void render(SpriteBatch sb) {
        if (!this.isHidden) {
            sb.setColor(Color.WHITE);
            sb.draw(ImageMaster.POTION_UI_SHADOW, this.x - 141.0f, this.y - 143.0f, 141.0f, 143.0f, 282.0f, 286.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 282, 286, false, false);
            sb.draw(ImageMaster.POTION_UI_BG, this.x - 141.0f, this.y - 143.0f, 141.0f, 143.0f, 282.0f, 286.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 282, 286, false, false);
            if (this.hbTop.hovered) {
                sb.draw(ImageMaster.POTION_UI_TOP, this.x - 141.0f, this.y - 143.0f, 141.0f, 143.0f, 282.0f, 286.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 282, 286, false, false);
            } else if (this.hbBot.hovered) {
                sb.draw(ImageMaster.POTION_UI_MID, this.x - 141.0f, this.y - 143.0f, 141.0f, 143.0f, 282.0f, 286.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 282, 286, false, false);
            }
            sb.draw(ImageMaster.POTION_UI_OVERLAY, this.x - 141.0f, this.y - 143.0f, 141.0f, 143.0f, 282.0f, 286.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 282, 286, false, false);
            Color c = Color.SKY;
            if (!MerchantsRug.canSell(relic)) {
                c = Color.GRAY;
            }
            FontHelper.renderFontCenteredWidth(sb, FontHelper.topPanelInfoFont, INFO_LABEL, this.x, this.y + 55.0f * Settings.scale, Settings.CREAM_COLOR);
            FontHelper.renderFontCenteredWidth(sb, FontHelper.topPanelInfoFont, SELL_LABEL, this.x, this.y - 2.0f * Settings.scale, c);
            this.hbTop.render(sb);
            this.hbBot.render(sb);
            if (this.hbBot.hovered) {
                float tipX = this.x > Settings.WIDTH * 0.75f ? this.x - (124.0f + 320.0f) * Settings.scale : this.x + 124.0f * Settings.scale;
                if (MerchantsRug.canSell(relic)) {
                    TipHelper.renderGenericTip(tipX, this.y + 50.0f * Settings.scale, SELL_LABEL, SELL_MESSAGE[0] + MerchantsRug.relicSalePrice(relic) + SELL_MESSAGE[1]);
                } else {
                    TipHelper.renderGenericTip(tipX, this.y + 50.0f * Settings.scale, CANT_SELL_LABEL, CANT_SELL_MESSAGE[0]);
                }
            }
        }
    }

}

