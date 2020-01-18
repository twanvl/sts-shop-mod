package shopmod.relics;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Astrolabe;
import com.megacrit.cardcrawl.relics.BloodyIdol;
import com.megacrit.cardcrawl.relics.CallingBell;
import com.megacrit.cardcrawl.relics.Cauldron;
import com.megacrit.cardcrawl.relics.DollysMirror;
import com.megacrit.cardcrawl.relics.EmptyCage;
import com.megacrit.cardcrawl.relics.Enchiridion;
import com.megacrit.cardcrawl.relics.GoldenIdol;
import com.megacrit.cardcrawl.relics.Mango;
import com.megacrit.cardcrawl.relics.Necronomicon;
import com.megacrit.cardcrawl.relics.NilrysCodex;
import com.megacrit.cardcrawl.relics.NlothsGift;
import com.megacrit.cardcrawl.relics.OddMushroom;
import com.megacrit.cardcrawl.relics.OldCoin;
import com.megacrit.cardcrawl.relics.Orrery;
import com.megacrit.cardcrawl.relics.Pear;
import com.megacrit.cardcrawl.relics.PotionBelt;
import com.megacrit.cardcrawl.relics.RedMask;
import com.megacrit.cardcrawl.relics.SpiritPoop;
import com.megacrit.cardcrawl.relics.TinyHouse;
import com.megacrit.cardcrawl.relics.Waffle;
import com.megacrit.cardcrawl.relics.WarPaint;
import com.megacrit.cardcrawl.relics.WarpedTongs;
import com.megacrit.cardcrawl.relics.Whetstone;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import basemod.patches.com.megacrit.cardcrawl.ui.panels.TopPanel.TopPanelPatches;
import shopmod.screens.RelicPopUp;

public class MerchantsRug extends CustomRelic {
    public static final String ID = "MerchantsRug";
    // Buying the rug itself
    public static final int BASE_PRICE = 150;
    //private static final float SALE_PROBABILITY = 0.33f;
    private static final float SALE_PROBABILITY = 1.0f;
    public static boolean forSale; // determined when ShopScreen is opened
    public static int price;
    public static float forSaleScale = Settings.scale;
    public static Hitbox rugHb = new Hitbox(1784.f * Settings.scale, Settings.HEIGHT - 445.f * Settings.scale, 105.f * Settings.scale, 150.f * Settings.scale);
    // Selling items
    public static float POTION_SALE_PRICE_MULTIPLIER = 0.33f;
    public static float RELIC_SALE_PRICE_MULTIPLIER = 0.25f;
    public static float PICKUP_RELIC_SALE_PRICE_MULTIPLIER = 0.25f;
    public static boolean CAN_SELL_SPECIAL_RELICS = false;
    public static boolean CAN_SELL_MERCHANTS_RUG = false;

    public MerchantsRug() {
        super(ID, ImageMaster.loadImage("img/relics/MerchantsRug.png"), ImageMaster.loadImage("img/relics/outline/MerchantsRug.png"), AbstractRelic.RelicTier.SPECIAL, AbstractRelic.LandingSound.FLAT);
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new MerchantsRug();
    }

    @Override
    public int getPrice() {
        return BASE_PRICE;
    }

    @Override
    public void onEquip() {
        initSalePrices();
    }

    // For Merchant screen
    private static Texture MERCHANT_OBJECTS_NO_RUG_IMG;

    public static Texture getMerchantRugImage() {
        if (AbstractDungeon.player.hasRelic(ID)) {
            if (MERCHANT_OBJECTS_NO_RUG_IMG == null) {
                MERCHANT_OBJECTS_NO_RUG_IMG = ImageMaster.loadImage("img/shop/merchantObjectsNoRug.png");
            }
            return MERCHANT_OBJECTS_NO_RUG_IMG;
        } else {
            return ImageMaster.MERCHANT_RUG_IMG;
        }
    }

    // For use in PotionPopUp screen
    private static final UIStrings potionUiStrings = CardCrawlGame.languagePack.getUIString("shopmod:PotionPopUp");
    public static final String SELL_LABEL = potionUiStrings.TEXT[0];
    public static final String[] SELL_MESSAGE = {potionUiStrings.TEXT[1], potionUiStrings.TEXT[2]};

    public static void renderDiscardLabel(SpriteBatch sb, BitmapFont font, String msg, float x, float y, Color c, int slot, AbstractPotion potion) {
        if (canSell(slot,potion)) {
            FontHelper.renderFontCenteredWidth(sb, font, SELL_LABEL, x, y, Settings.CREAM_COLOR);
        } else {
            FontHelper.renderFontCenteredWidth(sb, font, msg, x, y, c);
        }
    }
    public static void renderDiscardLabelTip(float x, float y, String header, String body, int slot, AbstractPotion potion) {
        if (canSell(slot,potion)) {
            TipHelper.renderGenericTip(x, y, SELL_LABEL, SELL_MESSAGE[0] + potionSalePrice(slot,potion) + SELL_MESSAGE[1]);
        } else {
            TipHelper.renderGenericTip(x, y, header, body);
        }
    }

    // For use in AbstractRelic
    public static RelicPopUp relicPopUp = new RelicPopUp();

    // For use in ShopScreen

    private static Texture SHOP_NO_RUG_IMG;

    public static Texture getShopNoRugImage() {
        if (SHOP_NO_RUG_IMG == null) {
            SHOP_NO_RUG_IMG = ImageMaster.loadImage("img/shop/noRug.png");
        }
        return SHOP_NO_RUG_IMG;
    }

    public static void initRugForShopScreen() {
        initSalePrices();
        if (AbstractDungeon.player.hasRelic(ID)) {
            MerchantsRug.forSale = false;
        } else {
            initRugSale();
        }
    }


    // Buying the Merchant's Rug itself

    public static void initRugSale() {
        // is it for sale?
        if (!AbstractDungeon.player.hasRelic(ID) && AbstractDungeon.merchantRng.random(0.f, 1.f) < SALE_PROBABILITY) {
            MerchantsRug.forSale = true;
            MerchantsRug.price = MerchantsRug.BASE_PRICE;
            if (!Settings.isDailyRun) {
                MerchantsRug.price = MathUtils.round((float)MerchantsRug.price * AbstractDungeon.merchantRng.random(0.95f, 1.05f));
            }
        } else {
            MerchantsRug.forSale = false;
        }
    }

    // Selling things when having the rug

    public static boolean isSelling() {
        return AbstractDungeon.isScreenUp && isShopScreenUp() && AbstractDungeon.player.hasRelic(ID);
    }
    private static boolean isShopScreenUp() {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SHOP) {
            return true;
        } else if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD) {
            // This is to work around a hack from BaseMod, where TopPanel.render is patched to change AbstractDungeon.screen
            // the actual screen is stored in a private variable
            AbstractDungeon.CurrentScreen savedScreen = (AbstractDungeon.CurrentScreen)ReflectionHacks.getPrivateStatic(TopPanelPatches.RenderPatch.class, "saveScreen");
            return savedScreen == AbstractDungeon.CurrentScreen.SHOP;
        } else {
            return false;
        }
    }

    public static void initSalePrices() {
        if (!AbstractDungeon.player.hasRelic(ID)) return;
        // potion prices
        sellingPotions.clear();
        for (AbstractPotion potion : AbstractDungeon.player.potions) {
            sellingPotions.add(new SellingPotion(potion));
        }
        // relic prices
        sellingRelics.clear();
        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            sellingRelics.put(relic, new SellingRelic(relic));
        }
    }

    static class SellingPotion {
        AbstractPotion potion;
        int price;
        SellingPotion(AbstractPotion potion) {
            this.potion = potion;
            this.price = initSalePrice(potion);
        }
    }
    public static ArrayList<SellingPotion> sellingPotions = new ArrayList<>();
    private static int initSalePrice(AbstractPotion potion) {
        if (potion instanceof PotionSlot || !potion.canDiscard()) return -1;
        return MathUtils.round(potion.getPrice() * POTION_SALE_PRICE_MULTIPLIER * AbstractDungeon.merchantRng.random(0.95f, 1.05f));
    }
    public static int potionSalePrice(int slot, AbstractPotion potion) {
        // update sale prices if they are out of date
        if (slot < 0) return -1;
        if (slot >= sellingPotions.size()) {
            for (int i = sellingPotions.size() ; i < AbstractDungeon.player.potions.size() ; ++i) {
                sellingPotions.add(new SellingPotion(AbstractDungeon.player.potions.get(i)));
            }
        } else if (sellingPotions.get(slot) == null || sellingPotions.get(slot).potion != potion) {
            sellingPotions.set(slot,new SellingPotion(potion));
        }
        return sellingPotions.get(slot).price;
    }
    public static boolean canSell(int slot, AbstractPotion potion) {
        return isSelling() && potionSalePrice(slot,potion) > 0;
    }
    public static void sell(int slot, AbstractPotion potion) {
        int price = potionSalePrice(slot, potion);
        CardCrawlGame.sound.play("SHOP_PURCHASE");
        AbstractDungeon.topPanel.destroyPotion(slot);
        AbstractDungeon.player.gainGold(price);
        sellingPotions.set(slot,null);
    }

    static class SellingRelic {
        AbstractRelic relic;
        int price;
        SellingRelic(AbstractRelic relic) {
            this.relic = relic;
            this.price = initSalePrice(relic);
        }
    }
    static HashMap<AbstractRelic,SellingRelic> sellingRelics = new HashMap<>();

    private static int initSalePrice(AbstractRelic relic) {
        // Base price based on tier
        float basePrice;
        if (relic.relicId == SpiritPoop.ID) {
            basePrice = 1;
        } else if (relic.tier == AbstractRelic.RelicTier.BOSS) {
            basePrice = 500; // the base game values these at 999, which is a bit much
        } else if (relic.tier == AbstractRelic.RelicTier.SPECIAL) {
            basePrice = 200; // special relics are not that good, and can even be bad
        } else {
            basePrice = relic.getPrice();
        }
        // Some can not be sold or have a special price
        switch (relic.relicId) {
            case MerchantsRug.ID: {
                if (!CAN_SELL_MERCHANTS_RUG) return -1;
                break;
            }
            case SpiritPoop.ID: {
                basePrice = 1;
                break;
            }
            case Necronomicon.ID:
            case NilrysCodex.ID:
            case Enchiridion.ID:
            case WarpedTongs.ID:
            case NlothsGift.ID:
            case GoldenIdol.ID:
            case BloodyIdol.ID:
            case OddMushroom.ID:
            case RedMask.ID: {
                break; // sellable event relics, other event relics are not sellable
            }
            case "hubris:BottledHeart": {
                basePrice = 999;
                break;
            }
            case Astrolabe.ID:
            case CallingBell.ID:
            case Cauldron.ID:
            case DollysMirror.ID:
            case EmptyCage.ID:
            case Mango.ID:
            case OldCoin.ID:
            case Orrery.ID:
            case Pear.ID:
            case PotionBelt.ID:
            case TinyHouse.ID:
            case Waffle.ID:
            case WarPaint.ID:
            case Whetstone.ID: {
                // relics with an effect only on pickup are worth less
                basePrice *= PICKUP_RELIC_SALE_PRICE_MULTIPLIER;
            }
            default: {
                if (relic.tier == RelicTier.SPECIAL && !CAN_SELL_SPECIAL_RELICS) {
                    return -1; // Don't sell event relics
                }
            }
        }
        // unsellable?
        if (basePrice <= 0) return -1;
        return MathUtils.round(basePrice * RELIC_SALE_PRICE_MULTIPLIER * AbstractDungeon.merchantRng.random(0.95f, 1.05f));
    }
    public static int relicSalePrice(AbstractRelic relic) {
        SellingRelic sale = sellingRelics.get(relic);
        if (sale == null) return -1;
        return sale.price;
    }
    public static boolean canSell(AbstractRelic relic) {
        return isSelling() && relic.isObtained && relicSalePrice(relic) > 0;
    }
    public static void sell(AbstractRelic relic) {
        if (AbstractDungeon.player.loseRelic(relic.relicId)) {
            CardCrawlGame.sound.play("SHOP_PURCHASE");
            AbstractDungeon.player.gainGold(relicSalePrice(relic));
            AbstractDungeon.topPanel.adjustRelicHbs();
        }
    }

}

