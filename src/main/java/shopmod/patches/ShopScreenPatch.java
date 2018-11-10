package shopmod.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;

import basemod.ReflectionHacks;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import shopmod.relics.MerchantsRug;

public class ShopScreenPatch {
    public static final boolean REPLACEMENT_RUG_IMAGE = true;

    @SpirePatch(clz = ShopScreen.class, method = "initRelics")
    public static class InitRelics {
        public static void Postfix(ShopScreen self) {
            MerchantsRug.initRugForShopScreen();
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "applyDiscount")
    public static class ApplyDiscount {
        public static void Postfix(ShopScreen self, float multiplier, boolean affectPurge) {
            if (MerchantsRug.forSale) {
                MerchantsRug.price = MathUtils.round((float)MerchantsRug.price * multiplier);
            }
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "update")
    public static class Update {
        public static void Prefix(ShopScreen self) {
            // Note: do this before ShopScreen.updateCards, to make it possible to cancel clicks
            MerchantsRug.relicPopUp.update();
        }

        public static void Postfix(ShopScreen self) {
            if (MerchantsRug.forSale) {
                float rugY = (float)ReflectionHacks.getPrivate(self, ShopScreen.class, "rugY");
                float currentY = rugY + Settings.HEIGHT - 445.f * Settings.scale;
                MerchantsRug.rugHb.y = currentY;
                MerchantsRug.rugHb.cY = currentY + MerchantsRug.rugHb.height / 2.f;
                MerchantsRug.rugHb.update();
                if (MerchantsRug.rugHb.hovered) {
                    ReflectionHacks.setPrivate(self,ShopScreen.class,"somethingHovered",true);
                    self.moveHand(MerchantsRug.rugHb.cX - 210.0f * Settings.scale, MerchantsRug.rugHb.cY - 70.0f);
                    MerchantsRug.forSaleScale = 1.25f; // Note: don't combine with Settings.scale, do that in render code
                } else {
                    MerchantsRug.forSaleScale = MathHelper.scaleLerpSnap(MerchantsRug.forSaleScale, 1.0f);
                }
                if (MerchantsRug.rugHb.hovered && InputHelper.justClickedRight) {
                    AbstractRelic relic = RelicLibrary.getRelic(MerchantsRug.ID);
                    CardCrawlGame.relicPopup.open(relic);
                }
                if (MerchantsRug.rugHb.hovered && InputHelper.justClickedLeft) {
                    MerchantsRug.rugHb.clickStarted = true;
                }
                if (MerchantsRug.rugHb.clicked || MerchantsRug.rugHb.hovered && CInputActionSet.select.isJustPressed()) {
                    MerchantsRug.rugHb.clicked = false;
                    if (AbstractDungeon.player.gold >= MerchantsRug.price) {
                        AbstractRelic relic = RelicLibrary.getRelic(MerchantsRug.ID);
                        AbstractDungeon.player.loseGold(MerchantsRug.price);
                        CardCrawlGame.sound.play("SHOP_PURCHASE", 0.1f);
                        CardCrawlGame.metricData.addShopPurchaseData(relic.relicId);
                        AbstractDungeon.getCurrRoom().relics.add(relic);
                        relic.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size(), true);
                        relic.flash();
                        MerchantsRug.forSale = false;
                        self.playBuySfx();
                        self.createSpeech(ShopScreen.getBuyMsg());
                    } else {
                        self.playCantBuySfx();
                        self.createSpeech(ShopScreen.getCantBuyMsg());
                    }
                }
            }
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "render")
    public static class Render {
        // Don't render the rug
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                boolean first = true;
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("draw") && first) {
                        first = false;
                        if (REPLACEMENT_RUG_IMAGE) {
                            m.replace(
                                "if (com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.hasRelic(shopmod.relics.MerchantsRug.ID)) {" +
                                "    sb.draw(shopmod.relics.MerchantsRug.getShopNoRugImage(), 0.0f, this.rugY, (float)com.megacrit.cardcrawl.core.Settings.WIDTH, (float)com.megacrit.cardcrawl.core.Settings.HEIGHT); " +
                                "} else {" +
                                "    $_ = $proceed($$); " +
                                "}");
                        } else {
                            m.replace("if (!com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.hasRelic(shopmod.relics.MerchantsRug.ID)) { $_ = $proceed($$); }");
                        }
                    }
                }
            };
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "renderRelics")
    public static class RenderRelics {
        // Note: we render the sale tag during renderRelics, so the image is below the merchant's hand
        public static Texture rugForSaleImg = null;

        public static void Postfix(ShopScreen self, SpriteBatch sb) {
            // Draw rug sales tag
            if (MerchantsRug.forSale) {
                // replace sales tag
                float rugY = (float)ReflectionHacks.getPrivate(self, ShopScreen.class, "rugY");
                final float FOR_SALE_SCALE_Y = 1080.f/1136.f; // The rug image has height 1136, but the screen has heihght 1080
                final float FOR_SALE_IMG_X = 1772.0f * Settings.scale;
                final float FOR_SALE_IMG_Y = 651.f * FOR_SALE_SCALE_Y * Settings.scale;
                final int FOR_SALE_IMG_WIDTH = 122;
                final int FOR_SALE_IMG_HEIGHT = 168;
                final float SALE_TAG_PRICE_X = 1825.0f * Settings.scale;
                final float SALE_TAG_PRICE_Y = Settings.HEIGHT - 425.0f * Settings.scale;
                final float RELIC_GOLD_OFFSET_X = -56.0f * Settings.scale;
                final float RELIC_GOLD_OFFSET_Y = -100.0f * Settings.scale;
                final float RELIC_PRICE_OFFSET_X = 14.0f * Settings.scale;
                final float RELIC_PRICE_OFFSET_Y = -62.0f * Settings.scale;
                final float GOLD_IMG_WIDTH = (float)ImageMaster.UI_GOLD.getWidth() * Settings.scale;
                if (rugForSaleImg == null) {
                    rugForSaleImg = ImageMaster.loadImage("img/shop/rugForSaleTag.png");
                }
                sb.setColor(Color.WHITE);
                // Note: sb.draw with scale is fundamentally broken when putting Settings.scale into the forSaleScale,
                // because the code uses both scaled and unscaled coordinates together.
                // Note: the fix would be to use
                //   float worldOriginX = x + originX;
                // instead of
                //   float worldOriginX = x + originX * scale;
                sb.draw(rugForSaleImg, FOR_SALE_IMG_X, rugY + FOR_SALE_IMG_Y, 16.f * Settings.scale, FOR_SALE_IMG_HEIGHT/2 * Settings.scale, FOR_SALE_IMG_WIDTH * Settings.scale, FOR_SALE_IMG_HEIGHT * FOR_SALE_SCALE_Y * Settings.scale, MerchantsRug.forSaleScale, MerchantsRug.forSaleScale, 0.f, 0, 0, FOR_SALE_IMG_WIDTH, FOR_SALE_IMG_HEIGHT, false, false);
                // price
                sb.setColor(Color.WHITE);
                sb.draw(ImageMaster.UI_GOLD, SALE_TAG_PRICE_X + RELIC_GOLD_OFFSET_X, rugY + SALE_TAG_PRICE_Y + RELIC_GOLD_OFFSET_Y, GOLD_IMG_WIDTH, GOLD_IMG_WIDTH);
                Color color = Color.WHITE;
                if (MerchantsRug.price > AbstractDungeon.player.gold) {
                    color = Color.SALMON;
                }
                FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipHeaderFont, Integer.toString(MerchantsRug.price), SALE_TAG_PRICE_X + RELIC_PRICE_OFFSET_X, rugY + SALE_TAG_PRICE_Y + RELIC_PRICE_OFFSET_Y, color);
                MerchantsRug.rugHb.render(sb);
            }
            // relic sales
            MerchantsRug.relicPopUp.render(sb);
        }
    }

}
