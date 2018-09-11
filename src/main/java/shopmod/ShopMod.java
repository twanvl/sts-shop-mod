package shopmod;

import java.nio.charset.StandardCharsets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.localization.RelicStrings;

import basemod.BaseMod;
import basemod.helpers.RelicType;
import basemod.interfaces.EditRelicsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import shopmod.relics.MerchantsRug;

@SpireInitializer
public class ShopMod implements
        PostInitializeSubscriber,
        EditRelicsSubscriber,
        EditStringsSubscriber {
    public static final String MODNAME = "ShopMod";
    public static final String AUTHOR = "twanvl";
    public static final String DESCRIPTION = "Modifies the merchant's shop, allowing you to buy more items.";

    public ShopMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new ShopMod();
    }

    @Override
    public void receivePostInitialize() {
        Texture badgeTexture = new Texture("img/ShopModBadge.png");
        BaseMod.registerModBadge(badgeTexture, MODNAME, AUTHOR, DESCRIPTION, null);
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new MerchantsRug(), RelicType.SHARED);
    }

    @Override
    public void receiveEditStrings() {
        // Note: it seems that naming the files localization/eng/relics.json crashes slay the spire on startup
        BaseMod.loadCustomStrings(RelicStrings.class, loadJson("localization/eng/shop-mod-relics.json"));
    }
    private static String loadJson(String jsonPath) {
        return Gdx.files.internal(jsonPath).readString(String.valueOf(StandardCharsets.UTF_8));
    }
}

