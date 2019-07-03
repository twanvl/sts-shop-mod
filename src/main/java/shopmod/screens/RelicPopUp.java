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
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import shopmod.relics.MerchantsRug;

public class RelicPopUp {
    private AbstractRelic relic;
    public boolean isHidden = true;
    public boolean targetMode = false;
    private Hitbox hbTop = new Hitbox(286.0f * Settings.scale, 120.0f * Settings.scale);
    private Hitbox hbBot = new Hitbox(286.0f * Settings.scale, 90.0f * Settings.scale);
    private Color topHoverColor = new Color(0.5f, 0.9f, 1.0f, 0.0f);
    private Color botHoverColor = new Color(1.0f, 0.4f, 0.3f, 0.0f);
    private float x;
    private float y;
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("shopmod:RelicPopUp");
    private static final String INFO_LABEL = uiStrings.TEXT[0];
    private static final String SELL_LABEL = uiStrings.TEXT[1];
    public static final String[] SELL_MESSAGE = {uiStrings.TEXT[2],uiStrings.TEXT[3]};
    private static final String CANT_SELL_LABEL = uiStrings.TEXT[4];
    public static final String[] CANT_SELL_MESSAGE = {uiStrings.TEXT[5]};

    public void open(AbstractRelic relic) {
        this.topHoverColor.a = 0.0f;
        this.botHoverColor.a = 0.0f;
        AbstractDungeon.topPanel.selectPotionMode = false;
        this.relic = relic;
        this.x = relic.hb.cX;
        this.y = relic.hb.cY - 164.0f * Settings.scale;
        this.isHidden = false;
        this.hbTop.move(this.x, this.y + 44.0f * Settings.scale);
        this.hbBot.move(this.x, this.y - 76.0f * Settings.scale);
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
            sb.draw(ImageMaster.POTION_UI_BG, this.x - 200.0f, this.y - 169.0f, 200.0f, 169.0f, 400.0f, 338.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 400, 338, false, false);
            this.topHoverColor.a = this.hbTop.hovered ? 0.5f : MathHelper.fadeLerpSnap(this.topHoverColor.a, 0.0f);
            this.botHoverColor.a = this.hbBot.hovered ? 0.5f : MathHelper.fadeLerpSnap(this.botHoverColor.a, 0.0f);
            sb.setBlendFunction(770, 1);
            sb.setColor(this.topHoverColor);
            sb.draw(ImageMaster.POTION_UI_TOP, this.x - 200.0f, this.y - 169.0f, 200.0f, 169.0f, 400.0f, 338.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 400, 338, false, false);
            sb.setColor(this.botHoverColor);
            sb.draw(ImageMaster.POTION_UI_BOT, this.x - 200.0f, this.y - 169.0f, 200.0f, 169.0f, 400.0f, 338.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 400, 338, false, false);
            sb.setBlendFunction(770, 771);
            Color c = Settings.CREAM_COLOR;
            if (!MerchantsRug.canSell(relic)) {
                c = Color.GRAY;
            }
            FontHelper.renderFontCenteredWidth(sb, FontHelper.buttonLabelFont, INFO_LABEL, this.x, this.hbTop.cY + 4.0f * Settings.scale, Settings.CREAM_COLOR);
            FontHelper.renderFontCenteredWidth(sb, FontHelper.buttonLabelFont, SELL_LABEL, this.x, this.hbBot.cY + 12 * Settings.scale, c);
            this.hbTop.render(sb);
            this.hbBot.render(sb);
            if (this.hbBot.hovered) {
                float tipX = this.x > Settings.WIDTH * 0.75f ? this.x - 174.0f * Settings.scale : this.x + 174.0f * Settings.scale;
                if (MerchantsRug.canSell(relic)) {
                    TipHelper.renderGenericTip(tipX, this.y + 20.0f * Settings.scale, SELL_LABEL, SELL_MESSAGE[0] + MerchantsRug.relicSalePrice(relic) + SELL_MESSAGE[1]);
                } else {
                    TipHelper.renderGenericTip(tipX, this.y + 20.0f * Settings.scale, CANT_SELL_LABEL, CANT_SELL_MESSAGE[0]);
                }
            }
        }
    }

}

