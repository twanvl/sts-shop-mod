package shopmod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;

import basemod.ReflectionHacks;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import shopmod.relics.MerchantsRug;

public class PotionPopUpPatch {
    @SpirePatch(clz = PotionPopUp.class, method = "render")
    public static class Render {
        public static ExprEditor Instrument () {
            return new ExprEditor() {
                int rfcw = 0;
                int rgt = 0;
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("renderFontCenteredWidth")) {
                        if (rfcw++ == 1) {
                            m.replace(" shopmod.relics.MerchantsRug.renderDiscardLabel(sb, this.x, this.y, this.slot, this.potion); ");
                        }
                    } else if (m.getMethodName().equals("renderGenericTip")) {
                        if (rgt++ == 2) {
                            m.replace(" shopmod.relics.MerchantsRug.renderDiscardLabelTip(this.x, this.y, this.slot, this.potion); ");
                        }
                    }
                }
            };
        }
    }

    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    public static class Update {
        public static void Prefix(PotionPopUp self) {
            if (MerchantsRug.isSelling()) {
                int slot = (int)ReflectionHacks.getPrivate(self, PotionPopUp.class, "slot");
                AbstractPotion potion = (AbstractPotion)ReflectionHacks.getPrivate(self, PotionPopUp.class, "potion");
                int price = MerchantsRug.potionSalePrice(slot,potion);
                Hitbox hbBot = (Hitbox)ReflectionHacks.getPrivate(self, PotionPopUp.class, "hbBot");
                if ((hbBot.clicked || hbBot.hovered && CInputActionSet.select.isJustPressed()) && potion.canDiscard() && price > 0) {
                    CInputActionSet.select.unpress();
                    hbBot.clicked = false;
                    MerchantsRug.sell(slot, potion);
                    ReflectionHacks.setPrivate(self, PotionPopUp.class, "slot", -1);
                    ReflectionHacks.setPrivate(self, PotionPopUp.class, "potion", null);
                    self.close();
                }
            }
        }
    }
}