package shopmod;

import java.nio.charset.StandardCharsets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;

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
        String lang = getSupportedLanguage();
        BaseMod.loadCustomStrings(RelicStrings.class, loadJson("localization/"+lang+"/shop-mod-relics.json"));
        BaseMod.loadCustomStrings(UIStrings.class, loadJson("localization/"+lang+"/shop-mod-ui.json"));
    }
    private static String loadJson(String jsonPath) {
        return Gdx.files.internal(jsonPath).readString(String.valueOf(StandardCharsets.UTF_8));
    }
    public static String getSupportedLanguage() {
        switch (Settings.language) {
            default:
                return "eng";
        }
    }
}

